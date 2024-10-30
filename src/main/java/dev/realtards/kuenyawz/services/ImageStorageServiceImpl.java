package dev.realtards.kuenyawz.services;

import dev.realtards.kuenyawz.configurations.ApplicationProperties;
import dev.realtards.kuenyawz.dtos.image.ImageResourceDTO;
import dev.realtards.kuenyawz.dtos.image.ImageUploadDto;
import dev.realtards.kuenyawz.entities.Product;
import dev.realtards.kuenyawz.entities.ProductImage;
import dev.realtards.kuenyawz.exceptions.ResourceNotFoundException;
import dev.realtards.kuenyawz.exceptions.ResourceUploadException;
import dev.realtards.kuenyawz.repositories.ProductImageRepository;
import dev.realtards.kuenyawz.repositories.ProductRepository;
import dev.realtards.kuenyawz.utils.idgenerator.SnowFlakeIdGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageStorageServiceImpl implements ImageStorageService {

	private final ProductRepository productRepository;

	private final ApplicationProperties applicationProperties;
	private final SnowFlakeIdGenerator idGenerator;
	private final ProductImageRepository productImageRepository;

	private final String ROOT_TO_UPLOAD = "/src/main/resources/uploads";
	private String productImagesDir = "product-images";
	private Path uploadLocation;
	private Set<String> acceptedExtensions;

	@Override
	@PostConstruct
	public void init() {
		productImagesDir = applicationProperties.getProductImagesDir();
		try {
			uploadLocation = Path.of(System.getProperty("user.dir"), ROOT_TO_UPLOAD, productImagesDir)
				.normalize()
				.toAbsolutePath();
			log.info("Upload directory set at '{}'", uploadLocation);
			acceptedExtensions = Set.copyOf(applicationProperties.getAcceptedImageExtensions());
			if (!Files.exists(uploadLocation)) {
				log.info("Creating upload directory at: {}", uploadLocation);
				Files.createDirectories(uploadLocation);
			}
		} catch (IOException e) {
			log.error("Failed to create upload directory at {}", uploadLocation, e);
			throw new ResourceUploadException("Could not create upload directory");
		} catch (SecurityException e) {
			log.error("Permission denied to create upload directory at {}", uploadLocation, e);
			throw new ResourceUploadException("Permission denied to create upload directory");
		}
	}

	@Override
	public ImageResourceDTO store(Long productId, ImageUploadDto imageUploadDto) {
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " not found"));
		if (product.getImages().size() >= 3) {
			throw new ResourceUploadException("Product " + productId + " has reached the maximum number of images");
		}

		ImageResourceDTO imageResourceDTO = processImageStoring(product, imageUploadDto);
		return imageResourceDTO;
	}

	@Override
	public Resource loadAsResource(final Long productId, String resourceUri) {
		try {
			final long resourceId = Long.parseLong(resourceUri.split("\\.")[0]);

			String relativePath = productImageRepository.findByProduct_ProductIdAndProductImageId(productId, resourceId)
				.orElseThrow(() -> new ResourceNotFoundException("Resource '" + productId + "/" + resourceId + "' not found"))
				.getRelativePath();
			Path requestedPath = Path.of(uploadLocation.toString(), relativePath).normalize().toAbsolutePath();
			log.warn("Requested path: {}", requestedPath);
			Resource resource = new UrlResource(requestedPath.toUri());

			if (resource.exists() || resource.isReadable()) {
				log.info("Resource '{}' found, exists and readable", requestedPath);
				return resource;
			} else {
				log.warn("Resource '{}' not found", requestedPath);
				throw new ResourceNotFoundException("Resource '" + productId + "/" + resourceUri + "' not found");
			}
		} catch (NumberFormatException | MalformedURLException e) {
			throw new ResourceNotFoundException("Resource '" + productId + "/" + resourceUri + "' not found");
		}
	}

	@Override
	public void delete(Long productId, String resourceUri) {
		try {
			final long resourceId = Long.parseLong(resourceUri.split("\\.")[0]);
			ProductImage productImage = productImageRepository.findByProduct_ProductIdAndProductImageId(productId, resourceId)
				.orElseThrow(() -> new ResourceNotFoundException("Resource '" + productId + "/" + resourceId + "' not found"));

			Path requestedPath = Path.of(uploadLocation.toString(), productImage.getRelativePath()).normalize().toAbsolutePath();
			log.warn("Requested path for deletion: {}", requestedPath);
			Files.deleteIfExists(requestedPath);
			productImageRepository.delete(productImage);
		} catch (NumberFormatException | IOException e) {
			throw new ResourceNotFoundException("Resource '" + productId + "/" + resourceUri + "' not found");
		}
	}

	@Override
	public void deleteAllOfProduct(Long productId) {
		Path productDirectory = Paths.get(uploadLocation.toString(), productId.toString());
		try (Stream<Path> paths = Files.walk(productDirectory)) {
			paths
				.filter(Files::isRegularFile)
				.map(Path::toFile)
				.forEach(File::delete);
			Files.deleteIfExists(productDirectory);
		} catch (IOException e) {
			log.error("Failed to delete product directory for product {}", productId, e);
			throw new ResourceUploadException("Could not delete product directory for product " + productId);
		} catch (SecurityException e) {
			log.error("Permission denied to delete product directory for product {}", productId, e);
			throw new ResourceUploadException("Permission denied to delete product directory for product " + productId);
		}
		productImageRepository.deleteAllByProduct_ProductId(productId);
	}

	@Override
	public void deleteAll() {
		try (Stream<Path> paths = Files.walk(uploadLocation)) {
			paths
				.filter(Files::isRegularFile)
				.map(Path::toFile)
				.forEach(File::delete);
			Files.deleteIfExists(uploadLocation);

			// Recreate the upload directory
			Files.createDirectories(uploadLocation);
		} catch (IOException e) {
			log.error("Failed to delete upload directory", e);
			throw new ResourceUploadException("Could not delete upload directory");
		} catch (SecurityException e) {
			log.error("Permission denied to delete upload directory", e);
			throw new ResourceUploadException("Permission denied to delete upload directory");
		}
		productImageRepository.deleteAll();
	}

	@Override
	public String getImageUrl(Long productId, String resourceUri) {
		return applicationProperties.getBaseUrl() + "/api/v1/images/" + productId + "/" + resourceUri;
	}

	@Override
	public String getImageUrl(ProductImage productImage) {
		return getImageUrl(productImage.getProduct().getProductId(), productImage.getStoredFilename());
	}

	@Override
	public List<String> getImageUrls(Product product) {
		return product.getImages().stream()
			.map(this::getImageUrl)
			.toList();
	}

	// Helper / extracted methods

	private ImageResourceDTO processImageStoring(Product product, ImageUploadDto imageUploadDto) {
		final MultipartFile file = imageUploadDto.getFile();

		if (file.isEmpty()) {
			throw new ResourceUploadException("Cannot store empty file");
		}

		final String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
		final String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

		if (!acceptedExtensions.contains(fileExtension.toLowerCase())) {
			throw new ResourceUploadException(String.format("Invalid file extension '%s', accepted: %s",
				fileExtension, String.join(", ", acceptedExtensions)));
		}

		final Long generatedId = idGenerator.generateId();
		final String storedFilename = generatedId + "." + fileExtension;
		final Path productDirectory = Paths.get(uploadLocation.toString(), product.getProductId().toString());

		if (!productDirectory.toFile().exists()) {
			try {
				Files.createDirectories(Paths.get(uploadLocation.toString(), product.getProductId().toString()));
			} catch (IOException e) {
				log.error("Failed to create directory for product {}", product.getProductId(), e);
				throw new ResourceUploadException("Could not create directory for product " + product.getProductId());
			}
		}

		final Path destinationPath = productDirectory.resolve(storedFilename).normalize();

		if (!destinationPath.startsWith(productDirectory)) {
			throw new ResourceUploadException("Cannot store file outside product upload directory");
		}

		try (InputStream inputStream = file.getInputStream()) {
			// Copy file to the target location
			Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);

			// Get safe relative path
			Path relativePath = uploadLocation.relativize(destinationPath);

			// Save to database
			productImageRepository.save(ProductImage.builder()
				.productImageId(generatedId)
				.originalFilename(originalFilename)
				.storedFilename(storedFilename)
				.relativePath(relativePath.toString())
				.fileSize(file.getSize())
				.product(product)
				.build());

			return ImageResourceDTO.builder()
				.imageResourceId(generatedId)
				.originalFilename(originalFilename)
				.filename(storedFilename)
				.relativeLocation(relativePath.toString())
				.build();

		} catch (IOException e) {
			log.error("Failed to store file {}: {}", originalFilename, e.getMessage());
			throw new ResourceUploadException("Failed to store file " + originalFilename);
		}
	}
}
