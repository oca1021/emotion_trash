package com.ggomi.emotion_trash;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    
    @ApiResponse(responseCode = "200", description = "감정 버리기 상세 조회 성공")
    @ApiResponse(responseCode = "400", description = "조회된 아이디가 없음을 안내")
    @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    @Operation(summary = "감정 쓰레기통 상세조회", description = "감정 쓰레기통 상세조회")
    @GetMapping("/emotions/{id}")
    public ResponseEntity<?> findById (
        @Parameter(description = "아이디 조건을 적으세요", example = "1") @PathVariable("id") long id
    ) {
        
        logger.info("감정 정보 상세 조회 아이디::{}", id);

        String sql = "SELECT * FROM EMOTIONS WHERE ID = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            // resultSet : 쿼리실행한 결과물을 담는다.
            ResultSet resultSet = preparedStatement.executeQuery();
            
            // 다음 결과물이 있으면
            if(resultSet.next()) {
                // Map타입의 변수 선언<key의타입(컬럼명), value의 타입(컬럼의 타입)>
                Map<String, Object> result = new HashMap<>();
                result.put("ID", resultSet.getLong("ID"));
                result.put("CONTENT", resultSet.getString("CONTENT"));
                result.put("SUBJECT", resultSet.getString("SUBJECT"));
                result.put("USE_YN", resultSet.getString("USE_YN"));
                result.put("REG_DTM", resultSet.getTimestamp("REG_DTM"));
                result.put("MODI_DTM", resultSet.getTimestamp("MODI_DTM"));
            
                // 클라이언트한테 보낼 값을 ok안에 담는다.
                return ResponseEntity.ok(result);
            }
            
            return ResponseEntity.badRequest().body("해당 아이디를 가진 데이터가 없습니다.");
        } catch (Exception e) {
            logger.error("상세 정보 조회 실패::{}", e.getMessage());
            return ResponseEntity.internalServerError().body("상세 조회에 실패했습니다.");
        }
    }





}
