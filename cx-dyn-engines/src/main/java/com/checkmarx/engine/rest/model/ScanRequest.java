/*******************************************************************************
 * Copyright (c) 2017 Checkmarx
 * 
 * This software is licensed for customer's internal use only.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.checkmarx.engine.rest.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScanRequest {
	
	public enum ScanStatus {

		Unknown(0),
		New(1),
		PreScan(2),
		Queued(3),
		Scanning(4),
		Deleted(5), //FIXME: don't know what id is scan status deleted
		Finished(7),
		Canceled(8),
		Failed(9),
		SourcePullingAndDeployment(10);
		
		private int stageId;
		public int getStageId() {
			return stageId;
		}

		private ScanStatus(int stageId) {
			this.stageId = stageId;
		}
		
		public static ScanStatus from(long stageId) {
			for (ScanStatus state : ScanStatus.values()) {
				if (stageId == state.stageId)
					return state;
			}
			return ScanStatus.Unknown;
		}
		
		public static ScanStatus from(Stage stage) {
			return stage == null ? ScanStatus.Unknown : from(stage.getId());
		}
	}
	
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

	/**
	 * ScanRequest.Id field
	 */
	public long getId() {
		return id;
	}

	public String getRunId() {
		return runId;
	}
	
	@JsonIgnore
	public ScanStatus getStatus() {
		return ScanStatus.from(stage);
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
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final ScanRequest other = (ScanRequest) obj;
		return Objects.equals(this.id, other.id);
	}

	private String printLanguages() {
		final StringBuilder sb = new StringBuilder();
		getLanguages().forEach(lang -> sb.append(lang.toString() + ", "));
		return sb.toString();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("runId", runId)
				.add("status", getStatus())
				.add("project", project.getId())
				.add("engineId", engineId)
				.add("loc", loc)
				.add("dateCreated", dateCreated)	
				.omitNullValues()
				.toString();
	}
	
	public String toString(boolean includeAll) {
		if (!includeAll) return toString();

		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("runId", runId)
				.add("status", getStatus())
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
}
