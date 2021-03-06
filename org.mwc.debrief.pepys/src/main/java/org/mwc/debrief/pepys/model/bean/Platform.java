package org.mwc.debrief.pepys.model.bean;

import org.mwc.debrief.pepys.model.db.annotation.Filterable;
import org.mwc.debrief.pepys.model.db.annotation.Id;
import org.mwc.debrief.pepys.model.db.annotation.TableName;

@TableName(name = "Platforms")
public class Platform implements AbstractBean {

	@Id
	private String platform_id;
	@Filterable
	private String name;

	public Platform() {

	}

	public String getName() {
		return name;
	}

	public String getPlatform_id() {
		return platform_id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setPlatform_id(final String platform_id) {
		this.platform_id = platform_id;
	}

}
