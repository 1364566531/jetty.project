//
//  ========================================================================
//  Copyright (c) 1995-2012 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.FutureCallback;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class HttpTransportOverHttp implements HttpTransport
{
    private static final Logger logger = Log.getLogger(HttpTransportOverHttp.class);
    private final ByteBufferPool _bufferPool;
    private final HttpConfiguration _configuration;
    private final EndPoint _endPoint;
    private final HttpGenerator _generator;
    private HttpGenerator.ResponseInfo _info;

    public HttpTransportOverHttp(ByteBufferPool _bufferPool, HttpConfiguration _configuration, EndPoint _endPoint, HttpGenerator generator)
    {
        this._bufferPool = _bufferPool;
        this._configuration = _configuration;
        this._endPoint = _endPoint;
        this._generator = generator;
    }

    @Override
    public void commit(HttpGenerator.ResponseInfo info, ByteBuffer content, boolean complete) throws IOException
    {
        // TODO This is blocking!  One of the important use-cases is to be able to write large static content without a thread
        generate(info, content, complete);
        // TODO: Trick only needed by the current HttpGenerator, that always require a ResponseInfo object
        if (!complete)
            _info = info;
    }

    @Override
    public void write(ByteBuffer content, boolean complete) throws IOException
    {
        generate(_info, content, complete);
        // TODO: Trick only needed by the current HttpGenerator, that always require a ResponseInfo object
        if (complete)
            _info = null;
    }

    private void generate(HttpGenerator.ResponseInfo info, ByteBuffer content, boolean complete) throws IOException
    {
        ByteBuffer header = null;
        out: while (true)
        {
            HttpGenerator.Result result = _generator.generateResponse(info, header, content, complete);
            if (logger.isDebugEnabled())
                logger.debug("{} generate: {} ({},{},{})@{}",
                        this,
                        result,
                        BufferUtil.toSummaryString(header),
                        BufferUtil.toSummaryString(content),
                        complete,
                        _generator.getState());

            switch (result)
            {
                case NEED_HEADER:
                {
                    if (header != null)
                        _bufferPool.release(header);
                    header = _bufferPool.acquire(_configuration.getResponseHeaderSize(), false);
                    continue;
                }
                case NEED_CHUNK:
                {
                    if (header != null)
                        _bufferPool.release(header);
                    header = _bufferPool.acquire(HttpGenerator.CHUNK_SIZE, false);
                    continue;
                }
                case FLUSH:
                {
                    if (info.isHead())
                    {
                        BufferUtil.clear(content);
                        if (BufferUtil.hasContent(header))
                            blockingWrite(header);
                    }
                    else if (BufferUtil.hasContent(header))
                    {
                        blockingWrite(header, content);
                    }
                    else
                    {
                        blockingWrite(content);
                    }
                    continue;
                }
                case SHUTDOWN_OUT:
                {
                    _endPoint.shutdownOutput();
                    continue;
                }
                case DONE:
                {
                    break out;
                }
                case CONTINUE:
                {
                    break;
                }
                default:
                {
                    throw new IllegalStateException();
                }
            }
        }
    }

    private void blockingWrite(ByteBuffer... bytes) throws IOException
    {
        try
        {
            FutureCallback<Void> callback = new FutureCallback<>();
            _endPoint.write(null, callback, bytes);
            callback.get();
        }
        catch (InterruptedException x)
        {
            throw (IOException)new InterruptedIOException().initCause(x);
        }
        catch (ExecutionException x)
        {
            Throwable cause = x.getCause();
            if (cause instanceof IOException)
                throw (IOException)cause;
            else if (cause instanceof Exception)
                throw new IOException(cause);
            else
                throw (Error)cause;
        }
    }
}
