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

package ru.iris.noolite4j.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usb4java.Context;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;
import ru.iris.noolite4j.CommandType;

import java.nio.ByteBuffer;

/**
 * Передатчик комманд PC118 (PC1116, PC1132)
 * Базовый класс
 * Модели отличаются количеством доступных каналов
 * @link http://www.noo.com.by/adapter-noolite-pc.html
 */

public class PC11xx {

    private static final short VENDOR_ID = 5824; //0x16c0;
    private static final short PRODUCT_ID = 1503; //0x05df;
    private static final Logger LOGGER = LoggerFactory.getLogger(PC11xx.class.getName());
    private final Context context = new Context();
    protected short availableChannels = 8;
    protected byte sendRepeat = 1;

    public void open() throws LibUsbException {

        LOGGER.info("Открывается устройство PC11xx");

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
    }

    public void close() {
        LOGGER.info("Закрывается устройство PC11xx");
        LibUsb.exit(context);
    }

    public boolean turnOn(byte channel)
    {
        if(channel >= availableChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.info("Включается устройство на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        ByteBuffer buf = ByteBuffer.allocateDirect(8);
        buf.put((byte) 0x30);
        buf.put((byte) CommandType.TURN_ON.getCode());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    public boolean turnOff(byte channel)
    {
        if(channel >= availableChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        LOGGER.info("Выключается устройство на канале {}", (channel+1));

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        ByteBuffer buf = ByteBuffer.allocateDirect(8);
        buf.put((byte) 0x30);
        buf.put((byte) CommandType.TURN_OFF.getCode());
        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    public boolean setLevel(byte channel, byte level)
    {
        if(channel >= availableChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        /**
         * Отсчет каналов начинается с 0
         */
        channel -= 1;

        ByteBuffer buf = ByteBuffer.allocateDirect(8);
        buf.put((byte) 0x30);
        buf.put((byte) CommandType.SET_LEVEL.getCode());
        buf.put((byte) 1);

        if (level > 100)
        {
            LOGGER.info("Включается устройство на канале " + (channel+1));
            level = 100;
        }
        else if (level < 0)
        {
            LOGGER.info("Выключается устройство на канале " + (channel+1));
            level = 0;
        }
        else
        {
            LOGGER.info("Устанавливается уровень {} на канале {}", level, (channel+1));
        }

        buf.position(5);
        buf.put(level);

        buf.position(4);
        buf.put(channel);

        writeToHID(buf);

        return true;
    }

    public boolean bindChannel(byte channel)
    {
        if(channel >= availableChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        ByteBuffer buf = ByteBuffer.allocateDirect(8);
        buf.put((byte) 0x30);
        buf.put((byte) CommandType.BIND.getCode());
        buf.position(4);
        buf.put(channel);

        LOGGER.info("Включен режим привязки для канала " + (channel+1));

        writeToHID(buf);
        return true;
    }

    public boolean unbindChannel(byte channel)
    {
        if(channel >= availableChannels) {
            LOGGER.error("Максимальное количество каналов: " + availableChannels);
            return false;
        }

        ByteBuffer buf = ByteBuffer.allocateDirect(8);
        buf.put((byte) 0x30);
        buf.put((byte) CommandType.UNBIND.getCode());
        buf.position(4);
        buf.put(channel);

        LOGGER.info("Включен режим отвязки для канала " + (channel+1));

        writeToHID(buf);
        return true;
    }

    /**
     * Непосредственная запись в устройство
     * @param command буффер посылаемых данных
     */
    private void writeToHID(ByteBuffer command)
    {
        DeviceHandle handle = LibUsb.openDeviceWithVidPid(context, VENDOR_ID, PRODUCT_ID);

        if (handle == null)
        {
            LOGGER.error("Устройство PC11XX не найдено!");
            return;
        }

        if (LibUsb.kernelDriverActive(handle, 0) == 1)
        {
            LibUsb.detachKernelDriver(handle, 0);
        }

        int ret = LibUsb.setConfiguration(handle, 1);

        if (ret != LibUsb.SUCCESS)
        {
            LOGGER.error("Ошибка конфигурирования PC11XX");
            LibUsb.close(handle);
            if (ret == LibUsb.ERROR_BUSY)
            {
                LOGGER.error("Устройство PC11XX занято");
            }
            return;
        }

        LibUsb.claimInterface(handle, 0);

        LOGGER.debug("PC11XX содержимое буффера: " + command.get(0) + " " + command.get(1) + " " + command.get(2) + " " + command.get(3)
                + " " + command.get(4) + " " + command.get(5) + " " + command.get(6)
                + " " + command.get(7));

        //send n times
        for (int i = 0; i <= sendRepeat; i++)
            LibUsb.controlTransfer(handle, (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE), (byte) 0x9, (short) 0x300, (short) 0, command, 100L);

        LibUsb.attachKernelDriver(handle, 0);
        LibUsb.close(handle);
    }
}
