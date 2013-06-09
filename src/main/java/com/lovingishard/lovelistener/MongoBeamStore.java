package com.lovingishard.lovelistener;

import com.google.common.base.Throwables;
import com.mongodb.*;
import com.typesafe.config.Config;

import java.net.UnknownHostException;

/**
 *
 */
public class MongoBeamStore implements BeamWriter {
    final String host;
    final int port;
    final String dbName;
    final String collectionName;
    final String username;
    final String password;

    MongoClient client;
    DB db;
    DBCollection collection;

    public MongoBeamStore(Config topConf) {
        Config conf = topConf.getConfig("love-listener.mongodb");
        this.host = conf.getString("host");
        this.port = conf.getInt("port");
        this.dbName = conf.getString("dbName");
        this.collectionName = "beams";
        this.username = conf.getString("username");
        this.password = conf.getString("password");
    }

    public void connect() {
        try {
            client = new MongoClient(host, port);
        } catch (UnknownHostException e) {
            throw Throwables.propagate(e);
        }
        db = client.getDB(dbName);
        db.authenticate(username, password.toCharArray());
        collection = db.getCollection(collectionName);
    }

    public void disconnect() {
        client.close();
        client = null;
        db = null;
        collection = null;
    }

    @Override
    public void write(Beam beam) {
        if (collection != null) {
            BasicDBObject dbObject = new BasicDBObject()
                    .append("latitude", beam.getLatitude())
                    .append("longitude", beam.getLongitude())
                    .append("detail", beam.getDetail())
                    .append("time", beam.getTime())
                    .append("source", beam.getSource());

            collection.insert(dbObject);
        }
    }
}
