/*
 * Copyright 2014 Nikolay A. Viguro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.iris.noolite4j.gateway;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import ru.iris.noolite4j.watchers.CommandType;
import ru.iris.noolite4j.watchers.DataFormat;

import java.io.IOException;

public class HTTPCommand {

    private byte channel;
    private CommandType cmd;
    private byte br;
    private DataFormat fmt;
    private byte d0;
    private byte d1;
    private byte d2;
    private byte d3;

    /**
     * Шлет данные на сервер R1132
     * @return успешно или нет
     */
    public boolean send() {

        String buildUrl = "http://" + PR1132.getHost() + "/api.htm?ch=" + channel + "&cmd=" + cmd;

        if(br != 0)
            buildUrl += "&br=" + br;

        if(fmt != null)
            buildUrl += "&fmt=" + fmt;

        if(d0 != 0)
            buildUrl += "&d0=" + d0;

        if(d1 != 0)
            buildUrl += "&d1=" + d1;

        if(d2 != 0)
            buildUrl += "&d2=" + d2;

        if(d3 != 0)
            buildUrl += "&d3=" + d3;

        try
        {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(buildUrl);
            CloseableHttpResponse response = httpclient.execute(httpget);

            return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public CommandType getCmd() {
        return cmd;
    }

    public void setCmd(CommandType cmd) {
        this.cmd = cmd;
    }

    public DataFormat getFmt() {
        return fmt;
    }

    public void setFmt(DataFormat fmt) {
        this.fmt = fmt;
    }

    public byte getBr() {
        return br;
    }

    public void setBr(byte br) {
        this.br = br;
    }

    public byte getD0() {
        return d0;
    }

    public void setD0(byte d0) {
        this.d0 = d0;
    }

    public byte getD1() {
        return d1;
    }

    public void setD1(byte d1) {
        this.d1 = d1;
    }

    public byte getD2() {
        return d2;
    }

    public void setD2(byte d2) {
        this.d2 = d2;
    }

    public byte getD3() {
        return d3;
    }

    public void setD3(byte d3) {
        this.d3 = d3;
    }

    @Override
    public String toString() {
        return "HTTPCommand{" +
                "channel=" + channel +
                ", cmd=" + cmd +
                ", br=" + br +
                ", fmt=" + fmt +
                ", d0=" + d0 +
                ", d1=" + d1 +
                ", d2=" + d2 +
                ", d3=" + d3 +
                '}';
    }
}
