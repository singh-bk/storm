/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.storm.redis.bolt;

import java.util.Map;

import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.container.JedisAdvancedContainerBuilder;
import org.apache.storm.redis.common.container.JedisAdvancedInstanceContainer;
import org.apache.storm.redis.common.container.JedisCommandsInstanceContainer;

import redis.clients.jedis.Jedis;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.base.BaseRichBolt;

/**
 * AbstractRedisBolt class is for users to implement custom bolts which makes interaction with Redis.
 * <p/>
 * Due to environment abstraction, AbstractRedisBolt provides JedisCommands which contains only single key operations.
 * <p/>
 * Custom Bolts may want to follow this pattern:
 * <p><blockquote><pre>
 * JedisCommands jedisCommands = null;
 * try {
 *     jedisCommand = getInstance();
 *     // do some works
 * } finally {
 *     if (jedisCommand != null) {
 *         returnInstance(jedisCommand);
 *     }
 * }
 * </pre></blockquote>
 *
 */
// TODO: Separate Jedis / JedisCluster to provide full operations for each environment to users
public abstract class AbstractAdvancedRedisBolt extends BaseRichBolt {
    protected OutputCollector collector;

    private transient JedisAdvancedInstanceContainer container;

    private JedisPoolConfig jedisPoolConfig;
    private JedisClusterConfig jedisClusterConfig;

    /**
     * Constructor for single Redis environment (JedisPool)
     * @param config configuration for initializing JedisPool
     */
    public AbstractAdvancedRedisBolt(JedisPoolConfig config) {
        this.jedisPoolConfig = config;
    }

    /**
     * Constructor for Redis Cluster environment (JedisCluster)
     * @param config configuration for initializing JedisCluster
     */
    public AbstractAdvancedRedisBolt(JedisClusterConfig config) {
        this.jedisClusterConfig = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        // FIXME: stores map (stormConf), topologyContext and expose these to derived classes
        this.collector = collector;

        if (jedisPoolConfig != null) {
            this.container = JedisAdvancedContainerBuilder.build(jedisPoolConfig);
        } else if (jedisClusterConfig != null) {
        	//TODO - Not configured for redis on a cluster
            //this.container = JedisCommandsContainerBuilder.build(jedisClusterConfig);
        } else {
            throw new IllegalArgumentException("Jedis configuration not found");
        }
    }

    /**
     * Borrow JedisCommands instance from container.<p/>
     * JedisCommands is an interface which contains single key operations.
     * @return implementation of JedisCommands
     * @see JedisCommandsInstanceContainer#getInstance()
     */
    protected Jedis getInstance() {
        return this.container.getInstance();
    }

    /**
     * Return borrowed instance to container.
     * @param instance borrowed object
     */
    protected void returnInstance(Jedis instance) {
        this.container.returnInstance(instance);
    }
}
