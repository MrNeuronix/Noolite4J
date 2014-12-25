Noolite4J
===============
Java API к протоколу беспроводных устройств **Noolite**

[![Build Status](https://travis-ci.org/Neuronix2/Noolite4J.png?branch=master)](https://travis-ci.org/Neuronix2/Noolite4J)

Maven
----------------

    <dependency>
      <groupId>ru.iris</groupId>
      <artifactId>noolite4j</artifactId>
      <version>1.2.5</version>
    </dependency>

Сборка
-----------------
Для того, чтобы собрать библиотеку, Вам необходим **Apache Maven**

Выполните в каталоге библиотеки команду:

```
mvn package
```

В результате исполнения в папке **target** вы найдете сам jar-архив с библиотекой и папку **lib** с зависимостями.

Так же Вы можете [скачать последний скомпилированный релиз](https://github.com/Neuronix2/Noolite4J/releases)

## Использование
Библиотека предоставляет доступ к USB-адаптерам Noolite для ПК, таким как передатчик PC11xx (PC118, PC1116, PC1132), приемник RX2164 и Ethernet-шлюз PR1132.

Пример использования:

``` java
    public static void main(String[] args) {

               PR1132 pr = new PR1132();
               PC1116 pc = new PC1116();
               RX2164 rx = new RX2164();

               Watcher watcher = new Watcher() {
                   @Override
                   public void onNotification(Notification notification) {
                       System.out.println("----------------------------------");
                       System.out.println("RX2164 получил команду: ");
                       System.out.println("Устройство: " + notification.getChannel());
                       System.out.println("Команда: " + notification.getType().name());
                       System.out.println("Формат данных к команде: " + notification.getDataFormat().name());

                       // Передаются данные с датчика
                       if(notification.getType() == CommandType.TEMP_HUMI)
                       {
                           SensorType sensor = (SensorType)notification.getValue("sensortype");
                           BatteryState battery = (BatteryState)notification.getValue("battery");

                           System.out.println("Температура: " + notification.getValue("temp"));
                           System.out.println("Влажность: " + notification.getValue("humi"));
                           System.out.println("Тип датчика: " + sensor.name());
                           System.out.println("Состояние батареи: " + battery.name());

                           if(sensor == SensorType.PT111)
                           {
                              System.out.println("Обнаружен датчик температуры и влажности");
                           }
                           else if(sensor == SensorType.PT112)
                           {
                               System.out.println("Обнаружен датчик температуры");
                           }
                       }
                   }
               };

               byte channel = 1;
               byte level = 85;

               PR1132.setHost("192.168.10.20");

               pr.turnOn(channel);
               pr.turnOff(channel);

               List<Sensor> sensors = pr.getSensors();

               // Температура с первого сенсора в списке
               short temp = sensors.get(0).getTemperature();

               pc.open();

               pc.turnOn(channel);
               pc.turnOff(channel);
               pc.setLevel(channel, level);

               pc.close();

               rx.open();
               rx.addWatcher(watcher);
               rx.start();

    }
```

В скомпилированную библиотеку входят программы для тестирования приемника RX2164, передатчика PC11xx и Ethernet-шлюза PR1132.

Примеры использования передатчика:

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPC11xx "turnon" "1"
```

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPC11xx "turnoff" "1"
```

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPC11xx "setlevel" "1" "55"
```

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPC11xx "bind" "5"
```

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPC11xx "unbind" "5"
```

Примеры использования Ethenet-шлюза PR1132:

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPR1132 "192.168.10.20" "turnon" "1"
```

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPR1132 "192.168.10.20" "turnoff" "1"
```

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPR1132 "192.168.10.20" "setlevel" "1" "55"
```

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPR1132 "192.168.10.20" "bind" "5"
```

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestPR1132 "192.168.10.20" "unbind" "5"
```

Примеры использования приемника:

```
java -cp noolite4j-x.jar ru.iris.noolite4j.TestRX2164
```

## Лицензия
Apache 2.0
