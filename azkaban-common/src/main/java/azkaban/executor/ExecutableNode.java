/*
 * Copyright 2013 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package azkaban.executor;

import java.util.*;

import azkaban.flow.CommonJobProperties;
import azkaban.flow.Node;
import azkaban.utils.Props;
import azkaban.utils.PropsUtils;
import azkaban.utils.TypedMapWrapper;

/**
 * Base Executable that nodes and flows are based.
 */
public class ExecutableNode {
  public static final String ID_PARAM = "id";
  public static final String STATUS_PARAM = "status";
  public static final String STARTTIME_PARAM = "startTime";
  public static final String ENDTIME_PARAM = "endTime";
  public static final String UPDATETIME_PARAM = "updateTime";
  public static final String INNODES_PARAM = "inNodes";
  public static final String OUTNODES_PARAM = "outNodes";
  public static final String TYPE_PARAM = "type";
  public static final String PROPS_SOURCE_PARAM = "propSource";
  public static final String JOB_SOURCE_PARAM = "jobSource";
  public static final String OUTPUT_PROPS_PARAM = "outputProps";
  //public static final String LIMIT_HOSTS = "limitHosts";
  public static final String PRIORITY= "priority";
  public static final String ALARM = "alarm";
  public static final String AUTHOR = "author";
  public static final String COMMENT = "comment";

  private String id;
  private String type = null;
  private Status status = Status.READY;
  private long startTime = -1;
  private long endTime = -1;
  private long updateTime = -1;

  private int priority= 0;
  private String alarm = null;
  private String author = null;
  private String comment = null;

  // Path to Job File
  private String jobSource;
  // Path to top level props file
  private String propsSource;
  private Set<String> inNodes = new HashSet<String>();
  private Set<String> outNodes = new HashSet<String>();
  //private List<String> limitHosts = new ArrayList<String>();

  private Props inputProps;
  private Props outputProps;

  public static final String ATTEMPT_PARAM = "attempt";
  public static final String PASTATTEMPTS_PARAM = "pastAttempts";

  private int attempt = 0;
  private long delayExecution = 0;
  private ArrayList<ExecutionAttempt> pastAttempts = null;

  // Transient. These values aren't saved, but rediscovered.
  private ExecutableFlowBase parentFlow;

  public ExecutableNode(Node node) {
    this.id = node.getId();
    this.jobSource = node.getJobSource();
    this.propsSource = node.getPropsSource();
  }

  public ExecutableNode(Node node, ExecutableFlowBase parent) {
    this(node.getId(), node.getType(), node.getJobSource(), node
            .getPropsSource(), parent);
  }

  public ExecutableNode(String id, String type, String jobSource,
      String propsSource, ExecutableFlowBase parent) {
    this.id = id;
    this.jobSource = jobSource;
    this.propsSource = propsSource;
    this.type = type;
    setParentFlow(parent);
  }

  public ExecutableNode() {
  }

  public ExecutableFlow getExecutableFlow() {
    if (parentFlow == null) {
      return null;
    }

    return parentFlow.getExecutableFlow();
  }

  public void setParentFlow(ExecutableFlowBase flow) {
    this.parentFlow = flow;
  }

  public ExecutableFlowBase getParentFlow() {
    return parentFlow;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getAlarm() {
    return alarm;
  }

  public void setAlarm(String alarm) {
    this.alarm = alarm;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Status getStatus() {
    return status;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public long getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(long updateTime) {
    this.updateTime = updateTime;
  }

  public void addOutNode(String exNode) {
    outNodes.add(exNode);
  }

  public void addInNode(String exNode) {
    inNodes.add(exNode);
  }

  public Set<String> getOutNodes() {
    return outNodes;
  }

  public Set<String> getInNodes() {
    return inNodes;
  }

  /*public void setLimitHosts(Collection<String> limitHosts) {
    this.limitHosts = new ArrayList<String>(limitHosts);
  }

  public List<String> getLimitHosts() {
    return limitHosts;
  }*/

  public boolean hasJobSource() {
    return jobSource != null;
  }

  public boolean hasPropsSource() {
    return propsSource != null;
  }

  public String getJobSource() {
    return jobSource;
  }

  public String getPropsSource() {
    return propsSource;
  }

  public void setInputProps(Props input) {
    this.inputProps = input;
  }

  public void setOutputProps(Props output) {
    this.outputProps = output;
  }

  public Props getInputProps() {
    return this.inputProps;
  }

  public Props getOutputProps() {
    return outputProps;
  }

  public long getDelayedExecution() {
    return delayExecution;
  }

  public void setDelayedExecution(long delayMs) {
    delayExecution = delayMs;
  }

  public List<ExecutionAttempt> getPastAttemptList() {
    return pastAttempts;
  }

  public int getAttempt() {
    return attempt;
  }

  public void setAttempt(int attempt) {
    this.attempt = attempt;
  }

  public void resetForRetry() {
    ExecutionAttempt pastAttempt = new ExecutionAttempt(attempt, this);
    attempt++;

    synchronized (this) {
      if (pastAttempts == null) {
        pastAttempts = new ArrayList<ExecutionAttempt>();
      }

      pastAttempts.add(pastAttempt);
    }

    this.setStartTime(-1);
    this.setEndTime(-1);
    this.setUpdateTime(System.currentTimeMillis());
    this.setStatus(Status.READY);
  }

  public List<Object> getAttemptObjects() {
    ArrayList<Object> array = new ArrayList<Object>();

    for (ExecutionAttempt attempt : pastAttempts) {
      array.add(attempt.toObject());
    }

    return array;
  }

  public String getNestedId() {
    return getPrintableId(":");
  }

  public String getPrintableId(String delimiter) {
    if (this.getParentFlow() == null
        || this.getParentFlow() instanceof ExecutableFlow) {
      return getId();
    }
    return getParentFlow().getPrintableId(delimiter) + delimiter + getId();
  }

  public Map<String, Object> toObject() {
    Map<String, Object> mapObj = new HashMap<String, Object>();
    fillMapFromExecutable(mapObj);

    return mapObj;
  }

  protected void fillMapFromExecutable(Map<String, Object> objMap) {
    objMap.put(ID_PARAM, this.id);
    objMap.put(STATUS_PARAM, status.toString());
    objMap.put(STARTTIME_PARAM, startTime);
    objMap.put(ENDTIME_PARAM, endTime);
    objMap.put(UPDATETIME_PARAM, updateTime);
    objMap.put(TYPE_PARAM, type);
    objMap.put(ATTEMPT_PARAM, attempt);

    objMap.put(PRIORITY, priority);
    objMap.put(ALARM, alarm);
    objMap.put(AUTHOR, author);
    objMap.put(COMMENT, comment);

    if (inNodes != null && !inNodes.isEmpty()) {
      objMap.put(INNODES_PARAM, inNodes);
    }
    if (outNodes != null && !outNodes.isEmpty()) {
      objMap.put(OUTNODES_PARAM, outNodes);
    }

    if (hasPropsSource()) {
      objMap.put(PROPS_SOURCE_PARAM, this.propsSource);
    }
    if (hasJobSource()) {
      objMap.put(JOB_SOURCE_PARAM, this.jobSource);
    }

    if (outputProps != null && outputProps.size() > 0) {
      objMap.put(OUTPUT_PROPS_PARAM, PropsUtils.toStringMap(outputProps, true));
    }

    /*if (limitHosts != null && limitHosts.size() > 0) {
      objMap.put(LIMIT_HOSTS, limitHosts);
    }*/

    if (pastAttempts != null) {
      ArrayList<Object> attemptsList =
          new ArrayList<Object>(pastAttempts.size());
      for (ExecutionAttempt attempts : pastAttempts) {
        attemptsList.add(attempts.toObject());
      }
      objMap.put(PASTATTEMPTS_PARAM, attemptsList);
    }
  }

  @SuppressWarnings("unchecked")
  public void fillExecutableFromMapObject(
      TypedMapWrapper<String, Object> wrappedMap) {
    this.id = wrappedMap.getString(ID_PARAM);
    this.type = wrappedMap.getString(TYPE_PARAM);
    this.status = Status.valueOf(wrappedMap.getString(STATUS_PARAM));
    this.startTime = wrappedMap.getLong(STARTTIME_PARAM);
    this.endTime = wrappedMap.getLong(ENDTIME_PARAM);
    this.updateTime = wrappedMap.getLong(UPDATETIME_PARAM);
    this.attempt = wrappedMap.getInt(ATTEMPT_PARAM, 0);
    this.priority= wrappedMap.getInt(PRIORITY);
    this.alarm = wrappedMap.getString(ALARM);
    this.author = wrappedMap.getString(AUTHOR);
    this.comment = wrappedMap.getString(COMMENT);

    this.inNodes = new HashSet<String>();
    this.inNodes.addAll(wrappedMap.getStringCollection(INNODES_PARAM,
        Collections.<String> emptySet()));

    this.outNodes = new HashSet<String>();
    this.outNodes.addAll(wrappedMap.getStringCollection(OUTNODES_PARAM,
        Collections.<String> emptySet()));

    this.propsSource = wrappedMap.getString(PROPS_SOURCE_PARAM);
    this.jobSource = wrappedMap.getString(JOB_SOURCE_PARAM);

    Map<String, String> outputProps =
        wrappedMap.<String, String> getMap(OUTPUT_PROPS_PARAM);
    if (outputProps != null) {
      this.outputProps = new Props(null, outputProps);
    }

    //this.limitHosts = new ArrayList<String>();
    //this.limitHosts.addAll(wrappedMap.getStringCollection(LIMIT_HOSTS, Collections.<String> emptyList()));
    //System.out.println(String.format("creator hosts %s", Arrays.asList(limitHosts)));

    Collection<Object> pastAttempts =
        wrappedMap.<Object> getCollection(PASTATTEMPTS_PARAM);
    if (pastAttempts != null) {
      ArrayList<ExecutionAttempt> attempts = new ArrayList<ExecutionAttempt>();
      for (Object attemptObj : pastAttempts) {
        ExecutionAttempt attempt = ExecutionAttempt.fromObject(attemptObj);
        attempts.add(attempt);
      }

      this.pastAttempts = attempts;
    }
  }

  public void fillExecutableFromMapObject(Map<String, Object> objMap) {
    TypedMapWrapper<String, Object> wrapper =
        new TypedMapWrapper<String, Object>(objMap);
    fillExecutableFromMapObject(wrapper);
  }

  public Map<String, Object> toUpdateObject() {
    Map<String, Object> updatedNodeMap = new HashMap<String, Object>();
    updatedNodeMap.put(ID_PARAM, getId());
    updatedNodeMap.put(STATUS_PARAM, getStatus().getNumVal());
    updatedNodeMap.put(STARTTIME_PARAM, getStartTime());
    updatedNodeMap.put(ENDTIME_PARAM, getEndTime());
    updatedNodeMap.put(UPDATETIME_PARAM, getUpdateTime());

    updatedNodeMap.put(ATTEMPT_PARAM, getAttempt());

    if (getAttempt() > 0) {
      ArrayList<Map<String, Object>> pastAttempts =
          new ArrayList<Map<String, Object>>();
      for (ExecutionAttempt attempt : getPastAttemptList()) {
        pastAttempts.add(attempt.toObject());
      }
      updatedNodeMap.put(PASTATTEMPTS_PARAM, pastAttempts);
    }

    return updatedNodeMap;
  }

  public void applyUpdateObject(TypedMapWrapper<String, Object> updateData) {
    this.status =
        Status.fromInteger(updateData.getInt(STATUS_PARAM,
            this.status.getNumVal()));
    this.startTime = updateData.getLong(STARTTIME_PARAM);
    this.updateTime = updateData.getLong(UPDATETIME_PARAM);
    this.endTime = updateData.getLong(ENDTIME_PARAM);

    if (updateData.containsKey(ATTEMPT_PARAM)) {
      attempt = updateData.getInt(ATTEMPT_PARAM);
      if (attempt > 0) {
        updatePastAttempts(updateData.<Object> getList(PASTATTEMPTS_PARAM,
            Collections.<Object> emptyList()));
      }
    }
  }

  public void applyUpdateObject(Map<String, Object> updateData) {
    TypedMapWrapper<String, Object> wrapper =
        new TypedMapWrapper<String, Object>(updateData);
    applyUpdateObject(wrapper);
  }

  public void cancelNode(long cancelTime) {
    if (this.status == Status.DISABLED) {
      skipNode(cancelTime);
    } else {
      this.setStatus(Status.CANCELLED);
      this.setStartTime(cancelTime);
      this.setEndTime(cancelTime);
      this.setUpdateTime(cancelTime);
    }
  }

  public void skipNode(long skipTime) {
    this.setStatus(Status.SKIPPED);
    this.setStartTime(skipTime);
    this.setEndTime(skipTime);
    this.setUpdateTime(skipTime);
  }

  private void updatePastAttempts(List<Object> pastAttemptsList) {
    if (pastAttemptsList == null) {
      return;
    }

    synchronized (this) {
      if (this.pastAttempts == null) {
        this.pastAttempts = new ArrayList<ExecutionAttempt>();
      }

      // We just check size because past attempts don't change
      if (pastAttemptsList.size() <= this.pastAttempts.size()) {
        return;
      }

      Object[] pastAttemptArray = pastAttemptsList.toArray();
      for (int i = this.pastAttempts.size(); i < pastAttemptArray.length; ++i) {
        ExecutionAttempt attempt =
            ExecutionAttempt.fromObject(pastAttemptArray[i]);
        this.pastAttempts.add(attempt);
      }
    }
  }

  public int getRetries() {
    return inputProps.getInt("retries", 0);
  }

  public long getRetryBackoff() {
    return inputProps.getLong("retry.backoff", 0);
  }

  public List<String> getAlarmTells () {
    return  inputProps.getStringList("flow.alarm.tells", Collections.<String> emptyList());
  }
}
