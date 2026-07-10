package com.isoft.medalterguardian.aspect;

import com.isoft.medalterguardian.annotation.Log;
import com.isoft.medalterguardian.common.LoginRequest; // 引入登入請求的實體類
import com.isoft.medalterguardian.entity.SysLog;
import com.isoft.medalterguardian.service.SysLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private SysLogService sysLogService;

    @Pointcut("@annotation(com.isoft.medalterguardian.annotation.Log)")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        String currentUsername = "未知用戶";

        // 1. 嘗試從 SecurityContext 獲取 (這招對「登出」和「普通修改」有效)
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetails) {
                currentUsername = ((UserDetails) auth.getPrincipal()).getUsername();
            }
        } catch (Exception e) {
            // 忽略異常
        }

        // 【修復】針對「登入」請求的特殊攔截
        //  直接去翻找前端傳過來的參數确保提前获取登录者信息
        if ("未知用戶".equals(currentUsername)) {
            for (Object arg : point.getArgs()) {
                if (arg instanceof LoginRequest) {
                    // 精準抓出用戶在登入框輸入的帳號！
                    currentUsername = ((LoginRequest) arg).getUsername();
                    break;
                }
            }
        }

        long beginTime = System.currentTimeMillis();

        // 3. 執行實際的 API (例如 AuthController.login)
        Object result = point.proceed();

        long time = System.currentTimeMillis() - beginTime;

        // 4. 將抓到的名字傳遞去保存
        saveLog(point, time, currentUsername);

        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long time, String currentUsername) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SysLog sysLog = new SysLog();

        Log logAnnotation = method.getAnnotation(Log.class);
        if (logAnnotation != null) sysLog.setOperation(logAnnotation.value());

        sysLog.setMethod(joinPoint.getTarget().getClass().getName() + "." + signature.getName() + "()");
        sysLog.setParams(Arrays.toString(joinPoint.getArgs()));
        sysLog.setTime(time);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            sysLog.setIp(request.getRemoteAddr());
        }

        // 寫入最終確定下來的操作人名稱
        sysLog.setUsername(currentUsername);

        sysLogService.save(sysLog);
    }
}