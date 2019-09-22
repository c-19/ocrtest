/*
 * Copyright (c) 2018 - 2019, C19, all rights reserved.
 *
 * This software is licensed under under GPL-3.0-only or GPL-3.0-or-later, https://opensource.org/licenses/GPL-3.0
 */

package io.c19.ocr;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.*;

public class OcrFactoryV2
{

    private ExecutorService executor;

    private OcrConfig config;
    private int threads;

    private Semaphore available;
    private Ocr[] workers;
    private BlockingQueue<Ocr> availableWorkers;

    public OcrFactoryV2(int threads, OcrConfig config )
    {
        this.threads = threads;
        this.config = config;
    }

    public void start()
    {
        available = new Semaphore( threads, true );
        workers = new Ocr[threads];
        availableWorkers = new LinkedBlockingQueue<>();
        executor = Executors.newFixedThreadPool( threads );

        for( int i=0; i< threads; i++ )
        {
            Ocr ocr = new Ocr( config );
            ocr.init();
            workers[i] = ocr;
            availableWorkers.add( ocr );
        }
    }

    public void shutdown()
    {
        executor.shutdownNow();
        for( Ocr ocr: workers )
        {
            ocr.stop();
        }
    }

    public Future<String> processAsync( String image )
    {
        OcrParams params = new OcrParams( image );

        return processAsync( params );
    }

    public Future<String> processAsync( InputStream stream )
    {
        OcrParams params = new OcrParams( stream );

        return processAsync( params );
    }

    private Future<String> processAsync( OcrParams params )
    {
        return this.executor.submit( new OcrProcessorTask( params ) );
    }

    public String process( String image )
    {
        try
        {
            return processAsync(image).get();
        }
        catch( InterruptedException | ExecutionException e )
        {
            throw new RuntimeException( "Error processing", e );
        }
    }

    public String process( InputStream image )
    {
        try
        {
            return processAsync(image).get();
        }
        catch( InterruptedException | ExecutionException e )
        {
            throw new RuntimeException( "Error processing", e );
        }
    }

    private class OcrProcessorTask implements Callable<String>
    {
        private OcrParams params;

        private OcrProcessorTask( OcrParams params )
        {
            this.params = params;
        }

        @Override
        public String call()
        {
            Ocr processor = null;
            try
            {
                available.acquire();
                processor = availableWorkers.take();
                return params.isFile() ? processor.process(  params.file) : processor.process( params.image );
            }
            catch( InterruptedException e )
            {
                throw new RuntimeException( "Error during processing.", e );
            }
            finally
            {
                if( processor != null )
                {
                    availableWorkers.add( processor );
                }
                available.release();
            }
        }
    }

    private class OcrParams
    {
        private InputStream image;
        private String file;

        private OcrParams( String file )
        {
            this.file = file;
        }

        private OcrParams( InputStream image )
        {
            this.image = image;
        }

        private boolean isFile()
        {
            return this.file != null;
        }
    }

}
