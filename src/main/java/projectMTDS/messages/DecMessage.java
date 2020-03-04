package projectMTDS.messages;

import java.io.Serializable;

public class DecMessage implements Serializable {
	private static final long serialVersionUID = -4446019688110423558L;

	private final int amount;

	public DecMessage(int amount) {
		super();
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}

}