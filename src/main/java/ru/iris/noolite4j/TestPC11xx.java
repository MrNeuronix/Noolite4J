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


import ru.iris.noolite4j.sender.PC1116;

public class TestPC11xx {

   public static void main(String[] args)
   {
       PC1116 pc = new PC1116();
       pc.open();

       if(args[0].isEmpty() || args[1].isEmpty())
       {
           System.out.println("Нет команды или канала!");
           System.exit(-1);
       }

       byte channel = Byte.valueOf(args[1]);

       switch (args[0])
       {
           case "turnon":
               System.out.println("Включается нагрузка на канале " + channel);
               pc.turnOn(channel);
               break;

           case "turnoff":
               System.out.println("Выключается нагрузка на канале " + channel);
               pc.turnOff(channel);
               break;

           case "setlevel":

               if(args[2].isEmpty())
               {
                   System.out.println("Не указан уровень нагрузки!");
                   System.exit(-1);
               }

               byte level = Byte.valueOf(args[2]);

               System.out.println("Устанавливается уровень " + level + " нагрузки на канале {}" + channel);
               pc.setLevel(channel, level);
               break;

           case "bind":
               System.out.println("Включен режим привзяки для канала " + channel);
               pc.bindChannel(channel);
               break;

           case "unbind":
               System.out.println("Выключен режим привзяки для канала" + channel);
               pc.unbindChannel(channel);
               break;

           default:
               System.out.println("Команда не опознана!");
               System.exit(-1);
               break;
       }

   }

}
