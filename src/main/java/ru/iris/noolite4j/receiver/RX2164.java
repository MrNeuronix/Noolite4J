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

package ru.iris.noolite4j.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usb4java.Context;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;
import ru.iris.noolite4j.watchers.*;

import java.nio.ByteBuffer;

/**
 * Приемник комманд RX2164
 * @see <a href="http://www.noo.com.by/adapter-dlya-kompyutera-rx2164.html">http://www.noo.com.by/adapter-dlya-kompyutera-rx2164.html</a>
 */

public class RX2164 {

    private static final long READ_UPDATE_DELAY_MS = 200L;
    private static final short VENDOR_ID = 5824; // 0x16c0;
    private static final short PRODUCT_ID = 1500; // 0x05dc;
    private final Logger LOGGER = LoggerFactory.getLogger(RX2164.class.getName());
    private final Context context = new Context();
    private Watcher watcher = null;
    private byte availableChannels = 64;
    private boolean shutdown = false;
    private DeviceHandle handle;
    private boolean pause = false;

    /**
     * Тут задается класс-callback
     * @param watcher собственно сам класс
     */
    public void addWatcher(Watcher watcher)
    {
        this.watcher = watcher;
    }

    /**
     * Точка начала работы с приемником
     * @throws LibUsbException ошибка LibUSB
     */
    public void open() throws LibUsbException {

        LOGGER.debug("Открывается устройство RX2164");

        // Инициализируем контекст
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS)
        {
            try
            {
                throw new LibUsbException("Не удалось инициализировать libusb", result);
            }
            catch (LibUsbException e)
            {
                LOGGER.error("Не удалось инициализировать libusb: ", result);
                e.printStackTrace();
            }
        }

        handle = LibUsb.openDeviceWithVidPid(context, VENDOR_ID, PRODUCT_ID);

        if (handle == null)
        {
            LOGGER.error("Устройство RX2164 не найдено!");
            return;
        }

        if (LibUsb.kernelDriverActive(handle, 0) == 1)
        {
            LibUsb.detachKernelDriver(handle, 0);
        }

        int ret = LibUsb.setConfiguration(handle, 1);

        if (ret != LibUsb.SUCCESS)
        {
            LOGGER.error("Ошибка конфигурирования RX2164");
            LibUsb.close(handle);
            if (ret == LibUsb.ERROR_BUSY)
            {
                LOGGER.error("Устройство RX2164 занято");
            }
            return;
        }

        LibUsb.claimInterface(handle, 0);
    }

    /**
     * Завершение работы
     */
    public void close() {
        LOGGER.debug("Закрывается устройство RX2164");
        shutdown = true;

        if(handle != null)
            LibUsb.close(handle);

        LibUsb.exit(context);
    }

    /**
     * Начать получать данные
     */
    public void start()
    {
        LOGGER.debug("Запускается процесс получения данных на устройстве RX2164");

        new Thread(new Runnable() {
            @Override
            public void run() {

                int togl;

                // Для старта бурем такое число, которе точно не может быть в данный момент в TOGL
                int tmpTogl = -10000;

                ByteBuffer buf = ByteBuffer.allocateDirect(8);

                /**
                 * Главный цикл получения данных
                 */
                while (!shutdown) {

                    /**
                     * Пауза нужна для того чтобы иметь возможность записать буффер в устройство
                     * (привязка, отвязка)
                     * TODO возможно, есть решение лучше
                     */
                    if (!pause) {
                        LibUsb.controlTransfer(handle, (byte)(LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_IN), (byte)0x9, (short)0x300, (short)0, buf, 100L);
                    }

                    /**
                     * Сравниваем значение TOGL, чтобы понять, что пришла новая команда
                     */
                    togl = buf.get(0) & 63;

                    /**
                     * Получена новая команда
                     * TOGL может быть 0
                     */
                    if (togl != tmpTogl) {

                        Notification notification = new Notification();

                        notification.setBuffer(buf);

                        byte channel = (byte) (buf.get(1) + 1);
                        byte action = buf.get(2);
                        byte dataFormat = buf.get(3);

                        LOGGER.debug("Получена новая команда для RX2164");
                        LOGGER.debug("Значение TOGL: " + togl);
                        LOGGER.debug("Команда: " + CommandType.getValue(action).name());
                        LOGGER.debug("Канал: " + channel);

                        if (dataFormat == DataFormat.NO_DATA.ordinal()) {
                            LOGGER.debug("Количество данных к команде: нет");
                            notification.setDataFormat(DataFormat.NO_DATA);
                        } else if (dataFormat == DataFormat.ONE_BYTE.ordinal()) {
                            LOGGER.debug("Количество данных к команде: 1 байт");
                            notification.setDataFormat(DataFormat.ONE_BYTE);
                        }
                        else if (dataFormat == DataFormat.TWO_BYTE.ordinal()) {
                            LOGGER.debug("Количество данных к команде: 2 байта");
                            notification.setDataFormat(DataFormat.TWO_BYTE);
                        }
                        else if (dataFormat == DataFormat.FOUR_BYTE.ordinal()) {
                            LOGGER.debug("Количество данных к команде: 4 байта");
                            notification.setDataFormat(DataFormat.FOUR_BYTE);
                        }
                        else if(dataFormat == DataFormat.LED.ordinal()) {
                            LOGGER.debug("Количество данных к команде: формат LED");
                            notification.setDataFormat(DataFormat.LED);
                        }
                        else
                        {
                            LOGGER.debug("Количество данных к команде: неизвестный тип");
                            notification.setDataFormat(DataFormat.NO_DATA);
                        }

                        notification.setChannel(channel);

                        switch (CommandType.getValue(action))
                        {
                            case TURN_ON:
                                notification.setType(CommandType.TURN_ON);
                                watcher.onNotification(notification);
                                break;
                            case TURN_OFF:
                                notification.setType(CommandType.TURN_OFF);
                                watcher.onNotification(notification);
                                break;
                            case SET_LEVEL:
                                notification.setType(CommandType.SET_LEVEL);
                                notification.addData("level", buf.get(4));
                                LOGGER.debug("Уровень устройства: " + buf.get(4));
                                watcher.onNotification(notification);
                                break;
                            case SWITCH:
                                notification.setType(CommandType.SWITCH);
                                watcher.onNotification(notification);
                                break;
                            case SLOW_TURN_ON:
                                notification.setType(CommandType.SLOW_TURN_ON);
                                watcher.onNotification(notification);
                                break;
                            case SLOW_TURN_OFF:
                                notification.setType(CommandType.SLOW_TURN_OFF);
                                watcher.onNotification(notification);
                                break;
                            case STOP_DIM_BRIGHT:
                                notification.setType(CommandType.STOP_DIM_BRIGHT);
                                watcher.onNotification(notification);
                                break;
                            case REVERT_SLOW_TURN:
                                notification.setType(CommandType.REVERT_SLOW_TURN);
                                watcher.onNotification(notification);
                                break;
                            case RUN_SCENE:
                                notification.setType(CommandType.RUN_SCENE);
                                watcher.onNotification(notification);
                                break;
                            case RECORD_SCENE:
                                notification.setType(CommandType.RECORD_SCENE);
                                watcher.onNotification(notification);
                                break;
                            case BIND:
                                notification.setType(CommandType.BIND);

                                /**
                                 * Если привязывается датчик, то он передает дополнительно свой тип
                                 */
                                if(notification.getDataFormat() == DataFormat.ONE_BYTE)
                                {
                                    // Тип сенсора
                                    notification.addData("sensortype", SensorType.values()[(buf.get(4) & 0xff)]);
                                }

                                watcher.onNotification(notification);
                                break;
                            case UNBIND:
                                notification.setType(CommandType.UNBIND);
                                watcher.onNotification(notification);
                                break;
                            case SLOW_RGB_CHANGE:
                                notification.setType(CommandType.SLOW_RGB_CHANGE);
                                watcher.onNotification(notification);
                                break;
                            case SWITCH_COLOR:
                                notification.setType(CommandType.SWITCH_COLOR);
                                watcher.onNotification(notification);
                                break;
                            case SWITCH_MODE:
                                notification.setType(CommandType.SWITCH_MODE);
                                watcher.onNotification(notification);
                                break;
                            case SWITCH_SPEED_MODE:
                                notification.setType(CommandType.SWITCH_SPEED_MODE);
                                watcher.onNotification(notification);
                                break;
                            case BATTERY_LOW:
                                notification.setType(CommandType.BATTERY_LOW);
                                watcher.onNotification(notification);
                                break;
                            case TEMP_HUMI:
                                notification.setType(CommandType.TEMP_HUMI);

                                /**
                                 * Информация о температуре, типе датчика и состоянии батареи
                                 * размазана по 2 байтам
                                 */

                                int intTemp = ((buf.get(5) & 0x0f) << 8) + (buf.get(4) & 0xff);

                                if (intTemp >= 0x800)
                                {
                                    intTemp = intTemp - 0x1000;
                                }

                                // Приводим к градусам Цельсия
                                double temp = (double)intTemp / 10;

                                // Состояни батареи
                                notification.addData("battery", BatteryState.values()[(buf.get(5) >> 7) & 1]);

                                // Температура
                                notification.addData("temp", temp);

                                // Тип сенсора
                                notification.addData("sensortype", SensorType.values()[((buf.get(5) >> 4) & 7)]);

                                /**
                                 * В третьем байте данных хранится влажность
                                 */
                                notification.addData("humi", buf.get(6) & 0xff);

                                /**
                                 * В четвертом байте данных хранятся данные о состоянии аналогового датчика
                                 * По умолчанию - unsigned byte (255)
                                 */
                                notification.addData("analog", buf.get(7) & 0xff);

                                watcher.onNotification(notification);
                                break;

                            default:
                                LOGGER.error("Неизвестная команда: " + action);
                        }
                    }

                    /**
                     * Спим
                     */
                    try {
                        Thread.sleep(READ_UPDATE_DELAY_MS);
                    } catch (InterruptedException e) {
                        LOGGER.error("Ошибка: " + e.getMessage());
                        e.printStackTrace();
                    }

                    tmpTogl = togl;
                    buf.clear();
                }
            }
        }).start();
    }

    /**
     * Функция, используемая для привязки устройства
     * на определенный канал RX2164
     * @param channel канал, на которое будет привязано устройство
     */
    public void bindChannel(byte channel)
    {
        LOGGER.debug("Получена новая команда для RX2164 - запрос привязки на канал " + channel);

        if(channel > availableChannels-1)
        {
            LOGGER.error("Заданный канал больше максимального значения!");
            return;
        }

        ByteBuffer buf = ByteBuffer.allocateDirect(8);
        buf.put((byte) 1);
        buf.put(channel);

        pause = true;
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LibUsb.controlTransfer(handle, (byte)(LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_IN), (byte)0x9, (short)0x300, (short)0, buf, 100);
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pause = false;

        LOGGER.debug("Команда выполнена");
    }

    /**
     * Функция, используемая для отвязки устройства
     * с определенного канала RX2164
     * @param channel канал, с которого будет отвязано устройство
     */
    public void unbindChannel(byte channel)
    {
        LOGGER.debug("Получена новая команда для RX2164 - запрос отвязки с канала " + channel);

        if(channel > availableChannels-1)
        {
            LOGGER.error("Заданный канал больше максимального значения!");
            return;
        }

        LOGGER.error("Заданный канал больше максимального значения!");

        ByteBuffer buf = ByteBuffer.allocateDirect(8);
        buf.put((byte) 3);
        buf.put(channel);

        pause = true;
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LibUsb.controlTransfer(handle, (byte)(LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_IN), (byte)0x9, (short)0x300, (short)0, buf, 100);
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pause = false;

        LOGGER.debug("Команда выполнена");
    }

    /**
     * Функция, используемая для отвязки всех устройств RX2164
     */
    public void unbindAllChannels()
    {
        LOGGER.debug("Получена новая команда для RX2164 - отчистка всех привязок");

        ByteBuffer buf = ByteBuffer.allocateDirect(8);
        buf.put((byte) 4);

        pause = true;
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LibUsb.controlTransfer(handle, (byte)(LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_IN), (byte)0x9, (short)0x300, (short)0, buf, 100);
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pause = false;

        LOGGER.debug("Команда выполнена");
    }
}
