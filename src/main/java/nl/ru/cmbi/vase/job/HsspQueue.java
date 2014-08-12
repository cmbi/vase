/**
 * Copyright 2014 CMBI (contact: <Coos.Baakman@radboudumc.nl>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.ru.cmbi.vase.job;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.ru.cmbi.vase.tools.util.Config;

public class HsspQueue {

	private static Logger log = LoggerFactory.getLogger(HsspQueue.class);

	private static final File cacheDir = new File(Config.properties.getProperty("hsspcache"));


	private ThreadPoolExecutor executor;

	public HsspQueue(int nThreads) {

		executor = new ThreadPoolExecutor(nThreads, nThreads,
	        0L, TimeUnit.MILLISECONDS,
	        new LinkedBlockingQueue<Runnable>());

		loadCache();
	}

	private Map<String,HsspJob> jobs = new HashMap<String,HsspJob>();

	private void loadCache() {

		if(cacheDir!=null && cacheDir.isDirectory()) {
            for(File file : cacheDir.listFiles(pdbfilter)) {

                HsspJob job = new HsspJob(file);

                jobs.put(job.getUUID().toString(), job);

                if( !job.getHsspFile().isFile() && !job.getErrorFile().isFile() ) {

                    executor.execute(job);
                }
            }
		}
	}

	public String submit(String pdbContents) {

		HsspJob job = new HsspJob(pdbContents);

		if(!jobs.containsKey(job.getUUID().toString())) {

			jobs.put(job.getUUID().toString(), job);

			executor.execute(job);
		}

		return job.getUUID().toString();
	}

	public JobStatus getStatus(String id) {

		if(jobs.containsKey(id)) {

			return jobs.get(id).getStatus();
		}

		else return JobStatus.UNKNOWN;
	}

	private static final FilenameFilter pdbfilter = new FilenameFilter() {

        public boolean accept(File directory, String fileName) {

            return fileName.endsWith(".pdb.gz");
        }
	};

	private static final FilenameFilter hsspfilter = new FilenameFilter() {

        public boolean accept(File directory, String fileName) {

            return fileName.endsWith(".hssp.bz2");
        }
	};
}
