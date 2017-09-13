/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package java.util.function;

@FunctionalInterface
public interface BiConsumer<T, U> {
	void accept(T t, U u);

	default BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
		return (l, r) -> {
			accept(l, r);
			after.accept(l, r);
		};
	}
}

/*
Compiled from "BiConsumer.java"
public interface java.util.function.BiConsumer<T, U> {
  public abstract void accept(T, U);

  public java.util.function.BiConsumer<T, U> andThen(java.util.function.BiConsumer<? super T, ? super U>);
    Code:
       0: aload_0
       1: aload_1
       2: invokedynamic #1,  0              // InvokeDynamic #0:accept:(Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;)Ljava/util/function/BiConsumer;
       7: areturn

  private void lambda$andThen$0(java.util.function.BiConsumer, java.lang.Object, java.lang.Object);
    Code:
       0: aload_0
       1: aload_2
       2: aload_3
       3: invokeinterface #2,  3            // InterfaceMethod accept:(Ljava/lang/Object;Ljava/lang/Object;)V
       8: aload_1
       9: aload_2
      10: aload_3
      11: invokeinterface #2,  3            // InterfaceMethod accept:(Ljava/lang/Object;Ljava/lang/Object;)V
      16: return
}
 */