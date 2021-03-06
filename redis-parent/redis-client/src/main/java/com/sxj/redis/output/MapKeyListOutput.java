// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.sxj.redis.output;

import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sxj.redis.codec.RedisCodec;
import com.sxj.redis.protocol.CommandOutput;

/**
 * {@link List} of keys output.
 *
 * @param <K> Key type.
 *
 * @author Will Glozer
 */
public class MapKeyListOutput<K, V> extends CommandOutput<K, V, Set<K>>
{
    public MapKeyListOutput(RedisCodec<K, V> codec)
    {
        super(codec, new LinkedHashSet<K>());
    }
    
    @Override
    public void set(ByteBuffer bytes)
    {
        output.add(codec.decodeMapKey(bytes));
    }
}
