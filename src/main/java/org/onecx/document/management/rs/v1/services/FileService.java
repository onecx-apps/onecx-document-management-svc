package org.onecx.document.management.rs.v1.services;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.onecx.document.management.rs.v1.models.FileInfoDTO;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.quarkus.logging.Log;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class FileService {
    @Inject
    MinioClient minioClient;

    private static final String CLASS_NAME = "FileService";

    @Transactional
    public FileInfoDTO uploadFile(String path, File file, String bucket)
            throws IOException, ServerException, InsufficientDataException, NoSuchAlgorithmException, InternalException,
            InvalidResponseException, XmlParserException, InvalidKeyException, ErrorResponseException {
        Log.info(CLASS_NAME, "Entered uploadFile method", null);
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        String contentType = URLConnection.guessContentTypeFromStream(is);

        if (Objects.isNull(contentType)) {
            contentType = "application/octet-stream";
        }
        byte[] fileBytes = is.readAllBytes();
        uploadFileToObjectStorage(fileBytes, path, bucket.toLowerCase(Locale.ROOT), contentType);
        is.close();
        Log.info(CLASS_NAME, "Exited uploadFile method", null);
        return new FileInfoDTO(contentType, path, bucket.toLowerCase(Locale.ROOT));
    }

    public GetObjectResponse downloadFile(String path, String bucket) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {
        Log.info(CLASS_NAME, "Entered downloadFile method", null);
        Log.info(CLASS_NAME, "Exited downloadFile method", null);
        return downloadFileFromObjectStorage(path, bucket.toLowerCase(Locale.ROOT));
    }

    @Transactional
    public void deleteFile(String fileId, String bucketName) throws InvalidKeyException, ErrorResponseException,
            InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException,
            ServerException, XmlParserException, IllegalArgumentException, IOException {
        Log.info(CLASS_NAME, "Entered deleteFile method", null);
        deleteFileFromObjectStorage(fileId, bucketName);
        Log.info(CLASS_NAME, "Exited deleteFile method", null);
    }

    public void checkAndCreateBucket(String bucket) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {
        Log.info(CLASS_NAME, "Entered checkAndCreateBucket method", null);
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!found) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucket)
                            .build());
        }
        Log.info(CLASS_NAME, "Exited checkAndCreateBucket method", null);
    }

    private void uploadFileToObjectStorage(byte[] fileBytes, String object, String bucket, String contentType)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        Log.info(CLASS_NAME, "Entered uploadFileToObjectStorage method", null);
        checkAndCreateBucket(bucket);
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(object)
                .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                .contentType(contentType)
                .build());
        Log.info(CLASS_NAME, "Exited uploadFileToObjectStorage method", null);

    }

    private GetObjectResponse downloadFileFromObjectStorage(String object, String bucket) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Log.info(CLASS_NAME, "Entered downloadFileFromObjectStorage method", null);
        Log.info(CLASS_NAME, "Exited downloadFileFromObjectStorage method", null);
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(object)
                .build());
    }

    private void deleteFileFromObjectStorage(String objectId, String bucketName) throws InvalidKeyException,
            ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        Log.info(CLASS_NAME, "Entered deleteFileFromObjectStorage method", null);
        boolean objectExists = isObjectPresent(objectId, bucketName);
        if (objectExists) {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectId)
                    .build());
        } else {
            throw new FileNotFoundException(
                    String.format("The file '%s' is not present in the Minio bucket '%s'", objectId, bucketName));
        }
        Log.info(CLASS_NAME, "Exited deleteFileFromObjectStorage method", null);

    }

    private boolean isObjectPresent(String objectId, String bucketName) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectId).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
