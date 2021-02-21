package com.kotori316.dl;

import java.time.ZonedDateTime;
import java.util.List;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;

public class Main2 {

    public static void main(String[] args) throws CurseException {
        var time = ZonedDateTime.now();
        var projectIDs = List.of(282837, 291006, 320926);
        for (int id : projectIDs) {
            CurseAPI.project(id).ifPresent(p -> Output.appendCsv(time, p.name(), p.downloadCount()));
        }
        System.exit(0);
    }
}
