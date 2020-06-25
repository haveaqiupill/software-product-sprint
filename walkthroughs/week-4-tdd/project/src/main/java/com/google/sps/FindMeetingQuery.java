// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long meetingDuration = request.getDuration();
    Collection<String> attendees = request.getAttendees();
    PriorityQueue<TimeRange> result = new PriorityQueue<>(Collections.reverseOrder(TimeRange.ORDER_BY_START));

    result.add(TimeRange.WHOLE_DAY);

    if (attendees.isEmpty()) {
      return new ArrayList<>(result);
    } else if (meetingDuration > TimeRange.WHOLE_DAY.duration()) {
      return Collections.emptyList();
    }

    // Filter events that the meeting participants will attend
    List<TimeRange> relevantTimeRanges = events.stream()
            .filter(e -> e.getAttendees().stream().anyMatch(attendees::contains))
            .map(Event::getWhen)
            .collect(Collectors.toList());

    // Combine time ranges that have overlapping 
    // time periods using two Priority Queues
    PriorityQueue<TimeRange> earliestFirst = new PriorityQueue<>(TimeRange.ORDER_BY_START);
    PriorityQueue<TimeRange> latestFirst = new PriorityQueue<>(Collections.reverseOrder(TimeRange.ORDER_BY_END));
    earliestFirst.addAll(relevantTimeRanges);

    while (!earliestFirst.isEmpty()) {
      TimeRange nextTimeRange = earliestFirst.poll();

      if (!latestFirst.isEmpty()) {
        TimeRange prevTimeRange = latestFirst.poll();
        if (prevTimeRange.end() >= nextTimeRange.start()) {
          latestFirst.add(
                  TimeRange.fromStartEnd(
                          prevTimeRange.start(),
                          Math.max(nextTimeRange.end(), prevTimeRange.end()),
                          false));
        } else {
          latestFirst.add(prevTimeRange);
          latestFirst.add(nextTimeRange);
        }
      } else {
        latestFirst.add(nextTimeRange);
      }
    }

    earliestFirst.addAll(latestFirst);

    // Get the time periods that are available for the meetings
    while (!earliestFirst.isEmpty()) {
      TimeRange availableTimeRange = result.poll();
      TimeRange eventTimeRange = earliestFirst.poll();

      if (eventTimeRange.start() - availableTimeRange.start() >= meetingDuration) {
        result.add(TimeRange.fromStartEnd(
                availableTimeRange.start(),
                eventTimeRange.start(),
                false));
      }

      if (availableTimeRange.end() - eventTimeRange.end() >= meetingDuration) {
        result.add(TimeRange.fromStartEnd(
                eventTimeRange.end(),
                availableTimeRange.end(),
                false));
      }
    }

    return Stream.generate(result::poll)
            .limit(result.size())
            .sorted(TimeRange.ORDER_BY_START)
            .collect(Collectors.toList());

  }
}
