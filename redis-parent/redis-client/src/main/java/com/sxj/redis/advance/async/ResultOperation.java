/**
 * Copyright 2014 Nikita Koksharov, Nickolay Borbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sxj.redis.advance.async;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import com.sxj.redis.RedisAsyncConnection;

public abstract class ResultOperation<R, V> implements AsyncOperation<V, R>
{
    
    @Override
    public void execute(Promise<R> promise,
            RedisAsyncConnection<Object, V> async)
    {
        Future<R> future = execute(async);
        future.addListener(new ResultListener<V, R>(promise, async, this));
    }
    
    protected abstract Future<R> execute(RedisAsyncConnection<Object, V> async);
    
}
