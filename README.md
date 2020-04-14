# Spring Tool Demo 工具类库项目
> 解决在Spring 遇到的一些问题
- NodeLock 节点锁
- Logx 通过AOP 记录方法以及接口的响应时间
- Json Gson API 提炼，Gson 对比fastJson 等更加稳定
- SnowFlake 雪花算法Java 版本
- TransactionCore 手动事务 在复杂逻辑中会遇到的



# Gson 替换Spring 序列化
```java
@Configurable
public class MvcConfig {
    public Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(BigDecimal.class, (JsonSerializer<BigDecimal>) (value, type, jsonSerializationContext) -> {
                    if (value == null) {
                        return null;
                    }
                    return new JsonPrimitive(value.setScale(2, BigDecimal.ROUND_DOWN));
                })
                .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (value, type, jsonSerializationContext) -> {
                    if (value == null) {
                        return null;
                    }
                    final BigDecimal b = new BigDecimal(value);
                    return new JsonPrimitive(b.setScale(2, BigDecimal.ROUND_DOWN));
                })
                .registerTypeAdapter(Float.class, (JsonSerializer<Float>) (value, type, jsonSerializationContext) -> {
                    if (value == null) {
                        return null;
                    }
                    final BigDecimal b = new BigDecimal(value);
                    return new JsonPrimitive(b.setScale(2, BigDecimal.ROUND_DOWN));
                })
                .registerTypeAdapter(
                        OSSUrl.class, (JsonSerializer<OSSUrl>) (value, type, jsonSerializationContext) -> {
                    if (value == null) {
                        return null;
                    }
                    return new JsonPrimitive(ossCoreService.getUrl(value.toString()));
                })
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .disableHtmlEscaping().create();
    }


    @Bean
    public HttpMessageConverters httpMessageConverters() {
        final GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(gson());
        final List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<>();
        httpMessageConverters.add(gsonHttpMessageConverter);
        return new HttpMessageConverters(true, httpMessageConverters);
    }
}
```

# WebSessionResolver 注入身份信息
```java
class xxx implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new WebSessionResolver());
        }
    
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
        }
    
        @Override
        public void addCorsMappings(CorsRegistry registry) {
    
        }
}



public class WebSessionResolver implements HandlerMethodArgumentResolver {
    private final RedisUtils redisUtils;

    WebSessionResolver() {
        this.redisUtils = SpringContextUtils.getBean(RedisUtils.class);
    }

    @Override
    public boolean supportsParameter(@NotNull MethodParameter methodParameter) {
        return Objects.equals(methodParameter.getParameterType().getName(), USession.class.getName()) ||
                Objects.equals(methodParameter.getParameterType().getName(), ClientDevice.class.getName()) ||
                Objects.equals(methodParameter.getParameterType().getName(), TableDTO.class.getName());
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
                                  @NotNull NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {

        if (Objects.equals(methodParameter.getParameterType().getName(), ClientDevice.class.getName())) {
            return new ClientDevice(getIp(nativeWebRequest), nativeWebRequest.getHeader("user-agent"));
        } else if (Objects.equals(methodParameter.getParameterType().getName(), USession.class.getName())) {
            final String token = nativeWebRequest.getHeader("token");
            if (!redisUtils.exists(Config.R_N_API_LOGIN + token)) {
                return new USession();
            }
            return redisUtils.getJsonObject(Config.R_N_API_LOGIN + token, USession.class);
        } else if (Objects.equals(methodParameter.getParameterType().getName(), TableDTO.class.getName())) {

        }
        return null;
    }

    private static String getIp(NativeWebRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        String ip2 = request.getHeader("X-Forwarded-For");
        if (ip2 != null && (!"unknown".equalsIgnoreCase(ip2))) {
            ip = ip2;
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("http_client_ip");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim();
        }
        if (ip == null) {
            HttpServletRequest r = request.getNativeRequest(HttpServletRequest.class);
            if (r != null) {
                ip = r.getRemoteAddr();
            }
        }
        if (ip == null) {
            ip = "127.0.0.1";
        }
        return ip;
    }
}

```