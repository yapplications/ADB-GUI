package application;

import application.intentbroadcasts.IntentBroadcast;
import application.log.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class ADBHelper {
    public static boolean pull(String from, String to) {

        //to = to.replaceAll(" ", "_");
        //to = to.replaceAll(" ", "\\\\ ");
        /*byte ptext[] = new byte[0];
        try {
            ptext = to.getBytes("UTF-8");

        to = new String(ptext, "CP1252");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/

        String run ="pull " + from + " " + to + "";
        String result = AdbUtils.run(run);

        if (!result.trim().contains("100%")) {
            Logger.e(result + "\nto: " + to);
            return false;
        }

        return true;
    }

    public static String rm(String fileToDelete) {
        return AdbUtils.run("shell rm \"" + fileToDelete + "\"");
    }

    public static String killServer() {
        return AdbUtils.run("kill-server");
    }

    public static String openApp(String packageString) {
        String result = AdbUtils.run("shell monkey -p " + packageString + " 1");

        if (!result.contains("No activities found to run")) {
            result = null;
        }

        return result;
    }

    public static String runMonkey(String applicationName, int numberOfSteps, int throttle) {
        String result = AdbUtils.run("shell monkey -p " + applicationName + " -v --throttle " + throttle + " " + numberOfSteps);

        if (!result.contains("No activities found to run")) {
            result = null;
        }

        return result;
    }

    public static String kill(String packageName, String pid) {
        String result = AdbUtils.run("shell run-as " + packageName + " kill " + pid);

        if (!result.contains("not debuggable")) {
            result = null;
        }


        return result;
    }

    public static String clearData(String getSelectedAppPackage) {

        String result = AdbUtils.run("shell pm clear " + getSelectedAppPackage);

        if (result.contains("Success")) {
            result = null;
        }

        return result;
    }

    public static String install(String selectedApk) {

        String result = AdbUtils.run("install -r " + selectedApk + "");
        String[] split = result.split("\n");

        if (split.length > 0) {
            if (split[split.length - 1].contains("Failure") || split[split.length - 1].contains("Missing")) {
                return split[split.length - 1];
            } else {
                return null;
            }
        }

        return "Wired Error";
    }

    public static void sendInputText(String text) {
        String result = AdbUtils.run("shell input text \"" + text + "\"");
        Logger.d("shell input text " + text + " -> " + result);

    }

    public static String sendIntent(IntentBroadcast intentBroadcast) {
        String command = "shell am " + intentBroadcast.activityManagerCommand +
                (!intentBroadcast.action.equals("") ? " -a " + intentBroadcast.action : "") +
                (!intentBroadcast.data.equals("") ? " -d " + intentBroadcast.data : "") +
                (!intentBroadcast.mimeType.equals("") ? " -t " + intentBroadcast.mimeType : "") +
                (!intentBroadcast.category.equals("") ? " -c " + intentBroadcast.category : "") +
                (!intentBroadcast.component.equals("") ? " -n " + intentBroadcast.component : "") +
                "";

        Logger.d(command);

        String result = AdbUtils.run(command);

        if (!result.contains("Error:")) {
            result = null;
        }

        return result;
    }

    public static String connectDeviceToWifi() {
        String result = null;

        result = AdbUtils.run("shell ip -f inet addr show wlan0");
        Logger.d("shell ip " + result);

        String[] split = result.split("\n");
        String ip = null;
        for (String line : split) {
            if (line.contains("inet")) {

                String[] splitSpaces = line.split("\\s+");
                int i = 0;
                for (String word : splitSpaces) {
                    if (word.contains("inet")) {
                        break;
                    }
                    i++;
                }

                if (splitSpaces.length > i + 1) {
                    ip = splitSpaces[i + 1].split("/")[0];
                }

                Logger.d("connectDeviceToWifi: ip: " + ip);

                break;
            }
        }

        if (ip != null) {
            result = AdbUtils.run("tcpip 5555");
            Logger.d("tcpip " + result);
            if (result.contains("restarting")) {

                result = AdbUtils.run("connect " + ip + ":5555");
                Logger.d("connect " + result);

                if (result.contains("connected")) {
                    result = null;
                }

            }
        }
        return result;
    }

    public static String setDateTime(Calendar calendar) {
        String result = null;
        result = AdbUtils.run("shell settings put global auto_time 0");
        Logger.d(result);

        result = AdbUtils.run("shell settings put global auto_time_zone 0");
        Logger.d(result);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddhhmmyyyy.ss");

        String dateString = simpleDateFormat.format(calendar.getTime());

        result = AdbUtils.run("root");

        result = AdbUtils.run("shell date " + dateString);
        Logger.d("shell date " + dateString + " ---> " + result);

        if (!result.contains("bad date") && !result.contains("not permitted")){
            result = null;
        }

        return result;
    }

    public static Set <String> getPackages() {
        String result = AdbUtils.run("shell pm list packages");

        String[] split = result.split("\n");

        Set <String> packages = new HashSet();

        for (int i = 1; i < split.length; i++) {
            String packageName = split[i].replace("package:", "").trim();
            if (packageName.equals("android")) {
                continue;
            }

            packages.add(packageName);
        }

        return packages;
    }

    public static boolean isADBFound() {
        String result = AdbUtils.run("version");

        return result.startsWith("Android Debug Bridge");
    }
}
