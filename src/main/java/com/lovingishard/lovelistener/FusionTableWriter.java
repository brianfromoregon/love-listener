package com.lovingishard.lovelistener;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.FusiontablesScopes;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import lang.Loggers;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * TODO apply for maps api for business grant: http://www.google.com/earth/outreach/grants/software/mapsapi.html
 * TODO apply for fusion tables quota increase https://code.google.com/apis/console/#project:728792558238:quotas
 */
public class FusionTableWriter implements BeamWriter {
    static final Logger log = Loggers.contextLogger();

    final String tableId;
    final String appName;
    final String serviceEmail;
    final String p12ResourceName;
    final int queueSize = 2000;         // Size of queue used between calling thread and internal writer thread

    ArrayBlockingQueue<Beam> queue;

    public FusionTableWriter(Config topConf) {
        Config conf = topConf.getConfig("love-listener.fusionTables");
        tableId = conf.getString("tableId");
        appName = conf.getString("appName");
        serviceEmail = conf.getString("serviceEmail");
        p12ResourceName = conf.getString("p12ResourceName");
    }

    public void connect() {
        queue = new ArrayBlockingQueue<>(queueSize);
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = new GsonFactory();

            // service account credential (uncomment setServiceAccountUser for domain-wide delegation)
            GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(serviceEmail)
                    .setServiceAccountScopes(Collections.singleton(FusiontablesScopes.FUSIONTABLES))
                    .setServiceAccountPrivateKey(SecurityUtils.loadPrivateKeyFromKeyStore(
                            SecurityUtils.getPkcs12KeyStore(), getClass().getResourceAsStream(p12ResourceName), "notasecret",
                            "privatekey", "notasecret"))
                    .build();

            Fusiontables fusiontables = new Fusiontables.Builder(httpTransport, jsonFactory, credential).setApplicationName(appName).build();
            new BatchWriter(queue, fusiontables, tableId).start();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void write(Beam beam) {
        if (queue != null) {
            try {
                queue.put(beam);
            } catch (InterruptedException e) {
                throw new AssertionError();
            }
        }
    }

    static class BatchWriter extends Thread {
        final int snoozeMs = 60 * 1000;             // Duration to sleep and batch write requests between writes to fusion
        final int batchSize = Integer.MAX_VALUE;    // Max number of rows per write
        final BlockingQueue<Beam> queue;
        final ArrayList<Beam> batch = new ArrayList<>(batchSize);
        final Fusiontables fusiontables;
        final String tableId;

        BatchWriter(BlockingQueue<Beam> queue, Fusiontables fusiontables, String tableId) {
            setName("Fusion table batch writer");
            setDaemon(true);
            this.queue = queue;
            this.fusiontables = fusiontables;
            this.tableId = tableId;
        }

        @Override
        public void run() {
            while (true) {
                do {
                    try {
                        batch.add(queue.take());
                    } catch (InterruptedException e) {
                        throw new AssertionError();
                    }
                    queue.drainTo(batch, batchSize - 1);
                    String batchCsv = Joiner.on("\n").join(Lists.transform(batch, new Function<Beam, String>() {
                        @Override
                        public String apply(Beam beam) {
                            return String.format("%s\0%f,%f\0%s", Strings.nullToEmpty(beam.getMessage()).replace("\"", "\"\""),
                                    beam.getLatitude(), beam.getLongitude(), new DateTime(beam.getTime()));
                        }
                    }));
                    log.info(String.format("About to import %d rows", batch.size()));
                    batch.clear();
                    try {
                        ByteArrayContent content = ByteArrayContent.fromString("application/octet-stream", batchCsv);
                        Fusiontables.Table.ImportRows cmd = fusiontables.table().importRows(tableId, content);
                        cmd.setDelimiter("\0");
                        cmd.execute();
                    } catch (IOException e) {
                        log.error("Writing to fusion table", e);
                    }
                } while (queue.size() >= batchSize);

                try {
                    log.info("Taking a snooze");
                    Thread.sleep(snoozeMs);
                } catch (InterruptedException e) {
                    throw new AssertionError();
                }
            }
        }
    }
}
