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

package ru.iris.noolite4j;

import ru.iris.noolite4j.receiver.RX2164;
import ru.iris.noolite4j.sender.PC1116;
import ru.iris.noolite4j.watchers.CommandType;
import ru.iris.noolite4j.watchers.Notification;
import ru.iris.noolite4j.watchers.SensorType;
import ru.iris.noolite4j.watchers.Watcher;

public class Main {

   public static void main(String[] ARGV)
   {
       PC1116 pc = new PC1116();
       RX2164 rx = new RX2164();

       Watcher watcher = new Watcher() {
           @Override
           public void onNotification(Notification notification) {
               System.out.println("RX2164 получил команду: ");
               System.out.println("Устройство: " + notification.getChannel());
               System.out.println("Команда: " + notification.getType().name());
               System.out.println("Формат данных к команде: " + notification.getDataFormat().name());

               // Передаются данные с датчика
               if(notification.getType().equals(CommandType.TEMP_HUMI))
               {
                   //System.out.println("Тип датчика: " + notification.getSensorType().name());
                   System.out.println("Температура: " + notification.getValue("temp"));
                   System.out.println("Влажность: " + notification.getValue("humi"));
                   //System.out.println("Тип датчика: " + notification.getValue("sensorType"));
                   //System.out.println("Состояние батареи: " + notification.getValue("battery"));
                   System.out.println("Бинарная строка 0: " + String.format("%8s", Integer.toBinaryString(notification.getBuffer().get(4) & 0xFF)).replace(' ', '0'));
                   System.out.println("Бинарная строка 1: " + String.format("%8s", Integer.toBinaryString(notification.getBuffer().get(5) & 0xFF)).replace(' ', '0'));
                   System.out.println("Бинарная строка 2: " + String.format("%8s", Integer.toBinaryString(notification.getBuffer().get(6) & 0xFF)).replace(' ', '0'));
                   System.out.println("Бинарная строка 3: " + String.format("%8s", Integer.toBinaryString(notification.getBuffer().get(7) & 0xFF)).replace(' ', '0'));

                   //if(notification.getSensorType().equals(SensorType.PT111))
                   //{
                   //   System.out.println("Обнаружен датчик температуры и влажности");


                   //}
                   //else if(notification.getSensorType().equals(SensorType.PT112))
                   //{
                   //    System.out.println("Обнаружен датчик температуры");


                   //}
                   //else if(notification.getSensorType().equals(SensorType.PT112))
                   //{
                   //    System.out.println("Обнаружен датчик движения");


                   //}
               }
           }
       };

       byte channel = 1;
       byte level = 85;

       pc.open();
       pc.turnOn(channel);
       pc.turnOff(channel);
       pc.setLevel(channel, level);
       pc.close();

       rx.open();
       rx.addWatcher(watcher);
       rx.start();
   }

}
