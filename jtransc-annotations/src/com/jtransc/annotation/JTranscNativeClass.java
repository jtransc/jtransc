/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Haxe interoperability. This annotations allows to mark a class as a class that is defined in haxe.
 *
 * Inside that class you can mark methods as native.
 *
 * In those definitions you can use:
 * - Primitives
 * - java.lang.String that will be automatically converted to haxe String
 * - byte[] that will be converted to haxe.io.Bytes
 * - Other classes marked with @JTranscNativeClass
 * - Other classes marked with @HaxeNativeConversion that allows to define how to convert between java and haxe types
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface JTranscNativeClass {
    String value();
}