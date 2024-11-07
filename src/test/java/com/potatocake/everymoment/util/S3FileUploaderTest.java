package com.potatocake.everymoment.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.potatocake.everymoment.config.AwsS3Properties;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class S3FileUploaderTest {

    private S3FileUploader uploader;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private AwsS3Properties properties;

    @BeforeEach
    void setUp() {
        uploader = new S3FileUploader(properties);
        ReflectionTestUtils.setField(uploader, "amazonS3", amazonS3);
    }

    @Test
    @DisplayName("파일이 성공적으로 업로드된다.")
    void should_UploadFile_When_ValidInput() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        given(properties.bucket()).willReturn("test-bucket");
        given(amazonS3.getUrl(any(), any())).willReturn(new URL("https://example.com/test.jpg"));

        // when
        String url = uploader.uploadFile(file);

        // then
        assertThat(url).isEqualTo("https://example.com/test.jpg");
        then(amazonS3).should().putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("지원하지 않는 파일 형식이면 예외가 발생한다.")
    void should_ThrowException_When_UnsupportedFileType() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> uploader.uploadFile(file))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_TYPE);

        then(amazonS3).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("파일 업로드 실패시 예외가 발생한다.")
    void should_ThrowException_When_UploadFails() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        given(properties.bucket()).willReturn("test-bucket");
        given(amazonS3.putObject(any(PutObjectRequest.class)))
                .willThrow(new RuntimeException("Upload failed"));

        // when & then
        assertThatThrownBy(() -> uploader.uploadFile(file))
                .isInstanceOf(RuntimeException.class);
    }

}
