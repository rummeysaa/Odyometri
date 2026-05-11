package audiometry;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.nio.charset.StandardCharsets;

public class SerialManager {

    private SerialPort activePort;

    public String[] getAvailablePortNames() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] portNames = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            portNames[i] = ports[i].getSystemPortName();
        }
        return portNames;
    }

    public boolean connectAndTest(String portName, ResponseCallback callback) {
        if (activePort != null && activePort.isOpen()) {
            activePort.closePort();
        }

        activePort = SerialPort.getCommPort(portName);
        activePort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
        activePort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (activePort.openPort()) {
            activePort.addDataListener(new SerialPortDataListener() {
                private final StringBuilder messageBuffer = new StringBuilder();

                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;

                    byte[] readBuffer = new byte[activePort.bytesAvailable()];
                    int numRead = activePort.readBytes(readBuffer, readBuffer.length);

                    if (numRead > 0) {
                        String part = new String(readBuffer, 0, numRead, StandardCharsets.UTF_8);
                        messageBuffer.append(part);

                        if (messageBuffer.toString().contains("RESPONSE")) {
                            callback.onResponseReceived("SUCCESS: 'RESPONSE' signal received from patient button!");
                            messageBuffer.setLength(0);
                        }
                    }
                }
            });
            return true;
        }
        return false;
    }

    public interface ResponseCallback {
        void onResponseReceived(String message);
    }
}