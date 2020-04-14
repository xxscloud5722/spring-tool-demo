//package com.github.xxscloud.demo;
//
//import com.github.xxscloud.demo.SnowFlake;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//
//
//public final class ThreadInfo {
//    private static final ThreadLocal<ThreadContext> THREAD_CACHE = ThreadLocal.withInitial(() -> new ThreadContext("REQ" + SnowFlake.getId()));
//
//    @Data
//    @AllArgsConstructor
//    private static class ThreadContext {
//        private String id;
//    }
//
//    public static String getRequestId() {
//        return THREAD_CACHE.get().getId();
//    }
//}
