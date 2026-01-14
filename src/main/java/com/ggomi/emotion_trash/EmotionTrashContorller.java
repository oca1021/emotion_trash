package com.ggomi.emotion_trash;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "감정 쓰레기통 API", description = "감정을 여기에 버려두세요.")
@RestController
public class EmotionTrashContorller {
    // 로거 객체 생성
    private Logger logger = LoggerFactory.getLogger(EmotionTrashContorller.class);

    // DB 접근을 위한 객체
    private final DataSource dataSource;

    public EmotionTrashContorller(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @ApiResponse(responseCode = "201", description = "감정 버리기 성공")
    @ApiResponse(responseCode = "400", description = "밸리데이션 실패")
    @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    @Operation(summary = "감정 쓰레기통에 등록", description = "감정 쓰레기통에 등록")
    @PostMapping("/emotions")
    public ResponseEntity<?> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "감정 정보",
            required = true,
            content = @Content(schema = @Schema(example = "{\"content\":\"다들 나만 미워해\", \"subject\":\"불만\"}"))
        )
        @RequestBody Map<String, String> params
    ) {
        logger.info("감정 정보 등록 요청 수신::{}", params);

        // 파라미터 추출
        String content = params.get("content"); // 내용
        String subject = params.get("subject"); // 주제

        // 밸리데이션 체크
        // 필수 값 체크
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("내용(content)은 필수 값입니다.");
        }

        // 길이 체크
        if (content.length() > 1000) {
            return ResponseEntity.badRequest().body("내용(content)의 길이는 1000을 초과할 수 없습니다.");
        }
        if (subject != null && subject.length() > 100) {
            return ResponseEntity.badRequest().body("주제(subject)의 길이는 100을 초과할 수 없습니다.");
        }

        String sql = "INSERT INTO EMOTIONS (CONTENT, SUBJECT) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, content);
            preparedStatement.setString(2, subject);

            int createdCount = preparedStatement.executeUpdate();
            if (createdCount == 0) {
                return ResponseEntity.internalServerError().body("신규 등록에 실패했습니다.");
            }
            logger.debug("createdCount::{}", createdCount);

            logger.info("감정 정보 등록 완료::{}", createdCount);
            return ResponseEntity.status(201).body("신규 등록에 성공했습니다.");
        } catch (Exception e) {
            logger.error("감정 정보 등록 실패::{}", e.getMessage());
            return ResponseEntity.internalServerError().body("신규 등록에 실패했습니다.");
        }
    }
}
