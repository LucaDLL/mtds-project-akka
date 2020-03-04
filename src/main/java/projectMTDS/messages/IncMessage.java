package projectMTDS.messages;

import java.io.Serializable;

public class IncMessage implements Serializable {
	private static final long serialVersionUID = 3169449768254646491L;

	private final int amount;

	public IncMessage(int amount) {
		super();
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}
}