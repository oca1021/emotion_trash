package com.ggomi.emotion_trash;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
    @Operation(summary = "감정 쓰레기통 목록 조회", description = "감정 쓰레기통 목록 조회")
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
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
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
            }
            
            return ResponseEntity.badRequest().body("해당 아이디를 가진 데이터가 없습니다.");
        } catch (Exception e) {
            logger.error("상세 정보 조회 실패::{}", e.getMessage());
            return ResponseEntity.internalServerError().body("상세 조회에 실패했습니다.");
        }
    }

    @ApiResponse(responseCode = "200", description = "감정 버리기 상세 조회 성공")
    @ApiResponse(responseCode = "400", description = "조회된 아이디가 없음을 안내")
    @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    @Operation(summary = "감정 쓰레기통 목록 조회", description = "감정 쓰레기통 목록 조회")
    @GetMapping("/emotions")
    public ResponseEntity<?> findAll (
        @RequestParam(name = "content", required = false) String content,
        @RequestParam(name = "subject", required = false) String subject,
        @RequestParam(name = "useYn", required = false) String useYn,
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "sort", required = false) String sort
    ) {
        // sort = "ID,DESC"
        // sort = "CONTENT,ASC"
        logger.info("감정 정보 목록 조회::content={}, subject={}, useYn={}, page={}, size={}, sort={}", content, subject, useYn, page, size, sort);

        StringBuilder sql = new StringBuilder("SELECT * FROM EMOTIONS WHERE 1=1");
        if (content != null && !content.trim().isEmpty()) {
            sql.append(" AND CONTENT LIKE '%' || ? || '%'");
        }
        if (subject != null && !subject.trim().isEmpty()) {
            sql.append(" AND SUBJECT LIKE '%' || ? || '%'");
        }
        if (useYn != null && !useYn.trim().isEmpty()) {
            sql.append(" AND USE_YN = ?");
        }
        
        if (sort != null && !sort.trim().isEmpty()) {
            // sort = "CONTENT,ASC";
            String[] tokens = sort.split(","); // ["CONTENT", "ASC"]
            String column = tokens[0]; // CONTENT
            String direction = tokens[1]; // ASC

            // ORDER BY CONTENT ASC
            sql.append(" ORDER BY " + column + " " + direction);
        } else {
            sql.append(" ORDER BY ID DESC");
        }

        // [1 2 3 4 5] [6 7 8 9 10] [11 12 13 14 15]

        sql.append(" LIMIT ?");
        sql.append(" OFFSET ?");
        logger.debug("sql::{}", sql);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            if (content != null && !content.trim().isEmpty()) {
                preparedStatement.setString(parameterIndex, content);
                parameterIndex += 1;
            }
            if (subject != null && !subject.trim().isEmpty()) {
                preparedStatement.setString(parameterIndex, subject);
                parameterIndex += 1;
            }
            if (useYn != null && !useYn.trim().isEmpty()) {
                preparedStatement.setString(parameterIndex, useYn);
                parameterIndex += 1;
            }
            preparedStatement.setLong(parameterIndex, size);
            parameterIndex += 1;

            preparedStatement.setLong(parameterIndex, (size * (page - 1)));

            // resultSet : 쿼리실행한 결과물을 담는다.
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Map<String, Object>> reulsts = new ArrayList<>();
                
                // 다음 결과물이 있으면
                while (resultSet.next()) {
                    // Map타입의 변수 선언<key의타입(컬럼명), value의 타입(컬럼의 타입)>
                    Map<String, Object> result = new HashMap<>();
                    result.put("ID", resultSet.getLong("ID"));
                    result.put("CONTENT", resultSet.getString("CONTENT"));
                    result.put("SUBJECT", resultSet.getString("SUBJECT"));
                    result.put("USE_YN", resultSet.getString("USE_YN"));
                    result.put("REG_DTM", resultSet.getTimestamp("REG_DTM"));
                    result.put("MODI_DTM", resultSet.getTimestamp("MODI_DTM"));
                    
                    reulsts.add(result);
                }

                // 클라이언트한테 보낼 값을 ok안에 담는다.
                return ResponseEntity.ok(reulsts);
            }
        } catch (Exception e) {
            logger.error("목록 정보 조회 실패::{}", e.getMessage());
            return ResponseEntity.internalServerError().body("목록 조회에 실패했습니다.");
        }
    }


    @ApiResponse(responseCode = "200", description = "감정 버리기 수정 성공")
    @ApiResponse(responseCode = "400", description = "밸리데이션 실패")
    @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    @Operation(summary = "감정 쓰레기통 수정", description = "감정 쓰레기통 수정")
    @PutMapping("/emotions/{id}")
    public ResponseEntity<?> updateById(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "감정 정보 수정",
            required = true,
            content = @Content(schema = @Schema(example = "{\"content\":\"다들 나만 미워해\", \"subject\":\"불만\",\"useYn\":\"N\"}"))
        )
        @RequestBody Map<String, String> params, @Parameter(description = "아이디 조건을 적으세요", example = "1") @PathVariable("id") long id
    ) {
        logger.info("감정 정보 수정 정보 수신::{}", params);

        // 파라미터 추출
        String content = params.get("content"); // 내용
        String subject = params.get("subject"); // 주제
        String useYn = params.get("useYn"); // 사용여부

        // 밸리데이션 체크
        // 필수 값 체크
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("내용(content)은 필수 값입니다.");
        }
        // 사용여부 체크
        if (useYn == null || useYn.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("사용여부(useYn)는 필수 값입니다.");
        }
        // 사용여부 데이터 체크
        if ( !useYn.equals("Y") && !useYn.equals("N")) {
            return ResponseEntity.badRequest().body("사용여부(useYn)는 대문자 'Y' 또는 'N'만 입력 가능합니다.");
        }         
        // 길이 체크
        if (content.length() > 1000) {
            return ResponseEntity.badRequest().body("내용(content)의 길이는 1000을 초과할 수 없습니다.");
        }
        if (subject != null && subject.length() > 100) {
            return ResponseEntity.badRequest().body("주제(subject)의 길이는 100을 초과할 수 없습니다.");
        }

        String sql = "UPDATE EMOTIONS SET CONTENT = ?, SUBJECT = ?, USE_YN = ? WHERE ID = ? ";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, content);
            preparedStatement.setString(2, subject);
            preparedStatement.setString(3, useYn);
            preparedStatement.setLong(4, id);

            int updateCount = preparedStatement.executeUpdate();
            if (updateCount == 0) {
                return ResponseEntity.internalServerError().body("수정에 실패했습니다.");
            }
            logger.debug("updateCount::{}", updateCount);

            logger.info("감정 정보 수정 완료::{}", updateCount);
            return ResponseEntity.ok("수정에 성공했습니다.");
        } catch (Exception e) {
            logger.error("감정 정보 수정 실패::{}", e.getMessage());
            return ResponseEntity.internalServerError().body("수정에 실패했습니다.");
        }
    }


    @ApiResponse(responseCode = "200", description = "감정 버리기 부분 수정 성공")
    @ApiResponse(responseCode = "400", description = "밸리데이션 실패")
    @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    @Operation(summary = "감정 쓰레기통 부분 수정", description = "감정 쓰레기통 부분 수정")
    @PatchMapping("/emotions/{id}")
    public ResponseEntity<?> patchById(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "감정 정보 부분 수정",
            required = false,
            content = @Content(schema = @Schema(example = "{\"content\":\"다들 나만 미워해\", \"subject\":\"불만\",\"useYn\":\"N\"}"))
        )
        @RequestBody Map<String, String> params, @Parameter(description = "아이디 조건을 적으세요", example = "1") @PathVariable("id") long id
    ) {
        logger.info("감정 정보 부분 수정 정보 수신::{}", params);

        // 파라미터 추출
        String content = params.get("content"); // 내용
        String subject = params.get("subject"); // 주제
        String useYn = params.get("useYn"); // 사용여부

        // 밸리데이션 체크
        // 필수 값 체크
        if (content != null) {
            if (content.trim().isEmpty() ) {
                return ResponseEntity.badRequest().body("내용(content)은 필수 값입니다.");
            }
            if (content.length() > 1000) {
                return ResponseEntity.badRequest().body("내용(content)의 길이는 1000을 초과할 수 없습니다.");
            }
        }

        // 사용여부 체크
        if (useYn != null) {
            // 값에 공백이 있을 ㄷ경우
            if (useYn.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("사용여부(useYn)는 필수 값입니다.");
            }
            // 값이 Y 또는 N 이 아닌경우
            if ( !useYn.equals("Y") && !useYn.equals("N")) {
                return ResponseEntity.badRequest().body("사용여부(useYn)는 대문자 'Y' 또는 'N'만 입력 가능합니다.");
            }
        }

        // 주제 길이 체크
        if (subject != null && subject.length() > 100) {
            return ResponseEntity.badRequest().body("주제(subject)의 길이는 100을 초과할 수 없습니다.");
        }

        // 동적 쿼리 생성
        StringBuilder sql = new StringBuilder("UPDATE EMOTIONS SET MODI_DTM = CURRENT_TIMESTAMP");
        if (content != null) {
            sql.append(", CONTENT = ?");
        }
        if (useYn != null) {
            sql.append(", USE_YN = ?");
        }
        if (subject != null) {
            sql.append(", SUBJECT = ?");
        }
        sql.append(" WHERE ID = ?");

        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            if (content != null) {
                preparedStatement.setString(parameterIndex++, content);
            }
            if (useYn != null) {
                preparedStatement.setString(parameterIndex++, useYn);
            }
            if (subject != null) {
                preparedStatement.setString(parameterIndex++, subject);
            }
            preparedStatement.setLong(parameterIndex, id);

            int patchCount = preparedStatement.executeUpdate();
            if (patchCount == 0) {
                return ResponseEntity.internalServerError().body("부분 수정에 실패했습니다.");
            }
            logger.debug("updateCount::{}", patchCount);

            logger.info("감정 정보 부분 수정 완료::{}", patchCount);
            return ResponseEntity.ok("부분 수정에 성공했습니다.");
        } catch (Exception e) {
            logger.error("감정 정보 부분 수정 실패::{}", e.getMessage());
            return ResponseEntity.internalServerError().body("부분 수정에 실패했습니다.");
        }
    }

    @ApiResponse(responseCode = "200", description = "감정 버리기 삭제 성공")
    @ApiResponse(responseCode = "400", description = "밸리데이션 실패")
    @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    @Operation(summary = "감정 쓰레기통 삭제", description = "감정 쓰레기통 삭제")
    @DeleteMapping("/emotions/{id}")
    public ResponseEntity<?> delete( @Parameter(description = "아이디 조건을 적으세요", example = "1") @PathVariable("id") long id
    ) {
        String sql = "UPDATE EMOTIONS SET USE_YN = 'N' WHERE ID = ? ";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            int deleteCount = preparedStatement.executeUpdate();
            if (deleteCount == 0) {
                return ResponseEntity.internalServerError().body("삭제에 실패했습니다.");
            }
            logger.debug("deleteCount::{}", deleteCount);

            logger.info("감정 정보 삭제 완료::{}", deleteCount);
            return ResponseEntity.ok("삭제에 성공했습니다.");
        } catch (Exception e) {
            logger.error("감정 정보 삭제 실패::{}", e.getMessage());
            return ResponseEntity.internalServerError().body("삭제에 실패했습니다.");
        }
    }


}
