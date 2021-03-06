/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Created on 8.12.2011
 *
 * Copyright (c) 2011 Et netera, a.s. All rights reserved.
 * Intended for internal use only.
 * http://www.etnetera.cz
 */
package net.formio.validation;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Report after validating an object. Immutable.
 *
 * @author Radek Beran
 */
public class ValidationResult implements Serializable {
	private static final long serialVersionUID = 5798813572430003173L;
	
	private final Map<String, Set<ConstraintViolationMessage>> fieldMessages;
	private final Set<ConstraintViolationMessage> globalMessages;
	
	public static final ValidationResult empty = newEmptyValidationResult();
	
	public ValidationResult(Map<String, Set<ConstraintViolationMessage>> fieldMessages, Set<ConstraintViolationMessage> globalMessages) {
		if (fieldMessages == null) throw new IllegalArgumentException("field messages cannot be null");
		if (globalMessages == null) throw new IllegalArgumentException("global messages cannot be null");
		Map<String, Set<ConstraintViolationMessage>> fieldMsgCopy = new LinkedHashMap<String, Set<ConstraintViolationMessage>>();
		for (Map.Entry<String, Set<ConstraintViolationMessage>> entry : fieldMessages.entrySet()) {
			fieldMsgCopy.put(entry.getKey(), new LinkedHashSet<ConstraintViolationMessage>(entry.getValue()));	
		}
		
		this.fieldMessages = Collections.unmodifiableMap(fieldMsgCopy);
		this.globalMessages = Collections.unmodifiableSet(new LinkedHashSet<ConstraintViolationMessage>(globalMessages));
	}

	/**
	 * Validation was successful, without validation errors (warnings and infos can be present).
	 * @return
	 */
	public boolean isSuccess() {
		boolean errorFound = false;
		if (fieldMessages != null && !fieldMessages.isEmpty()) {
			for (Set<ConstraintViolationMessage> fieldMsgs : fieldMessages.values()) {
				for (ConstraintViolationMessage msg : fieldMsgs) {
					if (msg.getSeverity() == Severity.ERROR) {
						errorFound = true;
						break;
					}
				}
				if (errorFound) break;
			}
		}
		if (globalMessages != null && !globalMessages.isEmpty()) {
			for (ConstraintViolationMessage msg : globalMessages) {
				if (msg.getSeverity() == Severity.ERROR) {
					errorFound = true;
					break;
				}
			}
		}
		return !errorFound;
	}
	
	/**
	 * Returns lists of messages for individual fields.
	 * @return
	 */
	public Map<String, Set<ConstraintViolationMessage>> getFieldMessages() {
		return fieldMessages;
	}
	
	/**
	 * Returns list of global validation messages (not related to one field).
	 * @return
	 */
	public Set<ConstraintViolationMessage> getGlobalMessages() {
		return globalMessages;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("globalMessages {\n");
		for (ConstraintViolationMessage msg : globalMessages) {
			if (msg != null) {
				if (first) {
					first = false;
				} else {
					sb.append(",\n");
				}
				sb.append("  " + msg.toString());
			}
		}
		sb.append("\n}\n");
		sb.append("fieldMessages {\n");
		first = true;
		for (Map.Entry<String, Set<ConstraintViolationMessage>> e : fieldMessages.entrySet()) {
			if (first) {
				first = false;
			} else {
				sb.append(",\n");
			}
			sb.append("  " + e.getKey() + "=");
			boolean firstMsg = true;
			for (ConstraintViolationMessage msg : e.getValue()) {
				if (msg != null) {
					if (firstMsg) {
						firstMsg = false;
					} else {
						sb.append(";");
					}
					sb.append(msg.toString());
				}
			}
		}
		sb.append("\n}\n");
		return sb.toString();
	}
	
	private static final ValidationResult newEmptyValidationResult() {
		return new ValidationResult(
			Collections.unmodifiableMap(Collections.<String, Set<ConstraintViolationMessage>>emptyMap()),
			Collections.unmodifiableSet(Collections.<ConstraintViolationMessage>emptySet())
		);
	}
}
