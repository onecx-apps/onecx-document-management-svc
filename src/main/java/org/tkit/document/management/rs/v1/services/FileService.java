package org.tkit.document.management.rs.v1.services;

import java.io.*;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.tkit.document.management.rs.v1.models.FileInfoDTO;

import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class FileService {
    @Inject
    MinioClient minioClient;

    public FileInfoDTO uploadFile(String path, File file, String bucket)
            throws IOException, ServerException, InsufficientDataException, NoSuchAlgorithmException, InternalException,
            InvalidResponseException, XmlParserException, InvalidKeyException, ErrorResponseException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        String contentType = URLConnection.guessContentTypeFromStream(is);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        byte[] fileBytes = is.readAllBytes();
        uploadFileToObjectStorage(fileBytes, path, bucket.toLowerCase(Locale.ROOT), contentType);
        is.close();
        return new FileInfoDTO(contentType, path, bucket.toLowerCase(Locale.ROOT));
    }

    public GetObjectResponse downloadFile(String path, String bucket)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return downloadFileFromObjectStorage(path, bucket.toLowerCase(Locale.ROOT));
    }

    public void checkAndCreateBucket(String bucket)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!found) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucket)
                            //                         .region(minioConfig.getRegion())
                            .build());
        }
    }

    private void uploadFileToObjectStorage(byte[] fileBytes, String object, String bucket, String contentType)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        checkAndCreateBucket(bucket);
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(object)
                .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                .contentType(contentType)
                .build());
    }

    private GetObjectResponse downloadFileFromObjectStorage(String object, String bucket)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(object)
                .build());
    }

}
