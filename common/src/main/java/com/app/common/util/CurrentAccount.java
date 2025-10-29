package com.app.common.util;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;


@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "#this")
public @interface CurrentAccount {
}
