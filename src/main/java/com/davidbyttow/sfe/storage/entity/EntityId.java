package com.davidbyttow.sfe.storage.entity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EntityId {
  boolean auto() default false;
}
