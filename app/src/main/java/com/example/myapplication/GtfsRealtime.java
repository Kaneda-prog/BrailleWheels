package com.example.myapplication;

import java.net.URL;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

public class GtfsRealtime {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://dadosabertos.rio.rj.gov.br/apiTransporte/apresentacao/rest/index.cfm/obterTodasPosicoes");
        FeedMessage feed = FeedMessage.parseFrom(url.openStream());
        for (FeedEntity entity : feed.getEntityList()) {
            if (entity.hasTripUpdate()) {
                System.out.println(entity.getTripUpdate() + "loplop");
            }
        }
    }
}