package application;

public class ProcessUtils {

	public static String[] getEnviormentParams() {
		String[] envp = new String [System.getenv().size()];

		int i = 0;
		for (String s : System.getenv().keySet()){
			//System.out.println(s + " " + System.getenv().get(s));
			envp[i] = s + "=" + System.getenv().get(s);
			i++;
		}

		return envp;
	}

}
