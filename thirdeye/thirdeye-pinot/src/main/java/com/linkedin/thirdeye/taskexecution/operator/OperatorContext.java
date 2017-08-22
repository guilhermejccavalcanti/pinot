package com.linkedin.thirdeye.taskexecution.operator;

import com.linkedin.thirdeye.taskexecution.dag.ExecutionContext;
import com.linkedin.thirdeye.taskexecution.dag.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperatorContext implements ExecutionContext<ExecutionResult> {
  private NodeIdentifier nodeIdentifier;
  private Map<NodeIdentifier, List<ExecutionResult>> inputs = new HashMap<>();

  @Override
  public NodeIdentifier getNodeIdentifier() {
    return nodeIdentifier;
  }

  @Override
  public void setNodeIdentifier(NodeIdentifier nodeIdentifier) {
    this.nodeIdentifier = nodeIdentifier;
  }

  @Override
  public Map<NodeIdentifier, List<ExecutionResult>> getInputs() {
    return inputs;
  }

  @Override
  public void addResult(NodeIdentifier identifier, List<ExecutionResult> operatorResult) {
    inputs.put(identifier, operatorResult);
  }
}