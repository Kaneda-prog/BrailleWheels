package com.example.myapplication;

import java.net.URL;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

public class GtfsRealtime {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://transit.land/feed-registry/operators/o-75cj-fetranspor");
        FeedMessage feed = FeedMessage.parseFrom(url.openStream());
        for (FeedEntity entity : feed.getEntityList()) {
            if (entity.hasTripUpdate()) {
                System.out.println(entity.getTripUpdate() + "loplop");
            }
        }
    }
}