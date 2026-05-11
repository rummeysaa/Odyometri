package audiometry.hardware;

import com.fazecast.jSerialComm.SerialPort;
import java.util.ArrayList;
import java.util.List;

public class SerialPortManager {

    /**
     * Sistemdeki aktif seri portları (COM1, COM2 vb.) listeler.
     * @return Aktif seri port isimlerinin listesi
     */
    public static List<String> getAvailablePorts() {
        List<String> portList = new ArrayList<>();
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            portList.add(port.getSystemPortName());
        }
        return portList;
    }

    /**
     * Port detaylarını getiren metod.
     * @return Detaylı port isimlerinin listesi
     */
    public static List<String> getAvailablePortsDetailed() {
        List<String> portList = new ArrayList<>();
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            portList.add(port.getSystemPortName() + " - " + port.getDescriptivePortName());
        }
        return portList;
    }
}
