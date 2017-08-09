package com.checkmarx.engine.rest.model;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScanRequest {
	
	private static final String TZ = "America/Chicago";
	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	private long id;
	private String runId;
	private Stage stage;
	private String teamId;
	private Project project;
	private Long engineId;
	private Integer loc;
	private ProgramLanguage[] languages;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DATE_PATTERN, timezone=TZ)
	private DateTime dateCreated;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DATE_PATTERN, timezone=TZ)
	private DateTime queuedOn;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DATE_PATTERN, timezone=TZ)
	private DateTime engineStartedOn;
	@JsonProperty(value = "isIncremental")
	private boolean incremental;
	@JsonProperty(value = "isPublic")
	private boolean isPublic;
	private String origin;

	public ScanRequest() {

	}

	public long getId() {
		return id;
	}

	public String getRunId() {
		return runId;
	}

	public Stage getStage() {
		return stage;
	}

	public String getTeamId() {
		return teamId;
	}

	public Project getProject() {
		return project;
	}

	public Long getEngineId() {
		return engineId;
	}

	public Integer getLoc() {
		return loc;
	}

	public List<ProgramLanguage> getLanguages() {
		return Arrays.asList(languages);
	}

	public DateTime getDateCreated() {
		return dateCreated;
	}

	public DateTime getQueuedOn() {
		return queuedOn;
	}

	public DateTime getEngineStartedOn() {
		return engineStartedOn;
	}

	public boolean isIncremental() {
		return incremental;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public String getOrigin() {
		return origin;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("runId", runId)
				.add("stage", stage)
				.add("teamId", teamId)
				.add("project", project)
				.add("engineId", engineId)
				.add("loc", loc)
				.add("languages", printLanguages())
				.add("dateCreated", dateCreated)	
				.add("queuedOn", queuedOn)
				.add("engineStartedOn", engineStartedOn)
				.add("incremental", incremental)
				.add("isPublic", isPublic)
				.add("origin", origin)
				.omitNullValues()
				.toString();
	}
	
	private String printLanguages() {
		final StringBuilder sb = new StringBuilder();
		getLanguages().forEach(lang -> sb.append(lang.toString() + ", "));
		return sb.toString();
	}
}
