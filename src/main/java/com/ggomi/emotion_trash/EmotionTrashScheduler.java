package com.ggomi.emotion_trash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmotionTrashScheduler {
    // 로거 객체 생성
    private Logger logger = LoggerFactory.getLogger(EmotionTrashScheduler.class);

    // 초 분 시 일 월 요일
    @Scheduled(cron = "0 * * * * *")
    public void test() {
        logger.info("스케줄 실행");
    }
}
