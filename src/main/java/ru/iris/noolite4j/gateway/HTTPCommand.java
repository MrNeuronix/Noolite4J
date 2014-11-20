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

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.iris.noolite4j.watchers.CommandType;
import ru.iris.noolite4j.watchers.DataFormat;

import javax.xml.bind.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPCommand.class.getName());

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

        String buildUrl = "http://" + PR1132.getHost() + "/api.htm?ch=" + channel + "&cmd=" + cmd.ordinal();

        if(br != 0)
            buildUrl += "&br=" + (br & 0xff);

        if(fmt != null)
            buildUrl += "&fmt=" + fmt.ordinal();

        if(d0 != 0)
            buildUrl += "&d0=" + (d0 & 0xff);

        if(d1 != 0)
            buildUrl += "&d1=" + (d1 & 0xff);

        if(d2 != 0)
            buildUrl += "&d2=" + (d2 & 0xff);

        if(d3 != 0)
            buildUrl += "&d3=" + (d3 & 0xff);

        try
        {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(buildUrl);
            CloseableHttpResponse response = httpclient.execute(httpget);

            return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;

        } catch (IOException e)
        {
            LOGGER.error("Произошла ошибка при отправке команды PR1132: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Возвращает текущие значения сенсоров
     * @return список сенсоров
     * TODO требует проверки работоспособности
     */
    public List<Sensor> getSensors()
    {
        List<Sensor> sensors = new ArrayList<>();

        try
        {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("http://" + PR1132.getHost() + "/sens.xml");
            CloseableHttpResponse response = httpclient.execute(httpget);

            String body = EntityUtils.toString(response.getEntity());

            try {

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(body);

                doc.getDocumentElement().normalize();

                NodeList nList = doc.getDocumentElement().getChildNodes();

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp);

                    byte value = Byte.valueOf(nNode.getNodeValue());

                    /**
                     * Найдем канал и связанный с ним сенсор
                     */

                    Pattern channels = Pattern.compile("\\d");
                    Matcher channelMatch = channels.matcher(nNode.getNodeName());
                    channelMatch.find();

                    byte channel = Byte.valueOf(channelMatch.group());

                    Sensor sensor = sensors.get(channel);

                    if(sensor == null)
                        sensor = new Sensor();

                    sensor.setChannel(channel);

                    /**
                     * Определим тип данных
                     */

                    Pattern sensorType = Pattern.compile("\\w+");
                    Matcher sensorTypeMatch = sensorType.matcher(nNode.getNodeName());
                    sensorTypeMatch.find();

                    String type = sensorTypeMatch.group();

                    if(type.equals("snst"))
                    {
                        sensor.setTemperature(value);
                    }
                    else if(type.equals("snsh"))
                    {
                        sensor.setHumidity(value);
                    }
                    else if(type.equals("snt"))
                    {
                        sensor.setState(SensorState.values()[value]);
                    }

                    boolean isNew = true;

                    for(int i = 0; i < sensors.size(); i++)
                    {
                        if(sensors.get(i).getChannel() == sensor.getChannel()) {
                            sensors.remove(i);
                            sensors.add(i, sensor);
                            isNew = false;
                        }
                    }

                    if(isNew)
                    {
                        sensors.add(sensor);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Произошла разборе данных с датчиков на PR1132: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (IOException e)
        {
            LOGGER.error("Произошла ошибка при отправке команды PR1132: " + e.getMessage());
            e.printStackTrace();
        }

        return sensors;

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
