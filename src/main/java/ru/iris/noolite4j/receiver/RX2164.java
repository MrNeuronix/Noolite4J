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
import ru.iris.noolite4j.watchers.Notification;
import ru.iris.noolite4j.watchers.Watcher;

import java.nio.ByteBuffer;

/**
 * Приемник комманд RX2164
 * @link http://www.noo.com.by/adapter-dlya-kompyutera-rx2164.html
 */

public class RX2164 {

    private static final long READ_UPDATE_DELAY_MS = 500L;
    private static final short VENDOR_ID = 5824; // 0x16c0;
    private static final short PRODUCT_ID = 1500; // 0x05dc;
    private final Logger LOGGER = LoggerFactory.getLogger(RX2164.class.getName());
    private final Context context = new Context();
    private Watcher watcher = null;
    private short availableChannels = 64;
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
     * @throws LibUsbException
     */
    public void open() throws LibUsbException {

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

        DeviceHandle handle = LibUsb.openDeviceWithVidPid(context, VENDOR_ID, PRODUCT_ID);

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
        shutdown = true;
        LibUsb.exit(context);
    }

    /**
     * Начать получать данные
     */
    public void start()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {

                ByteBuffer tmpBuf = ByteBuffer.allocateDirect(8);

                /**
                 * Главный цикл получения данных
                 */
                while (!shutdown) {
                    ByteBuffer buf = ByteBuffer.allocateDirect(8);
                    if (!pause) {
                        LibUsb.controlTransfer(handle, (byte)(LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_IN), (byte)0x9, (short)0x300, (short)0, buf, 100L);
                    }

                }
            }
        });
    }


}
