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

/**
 * Fetches comments from the servers and adds them to the DOM.
 */
function getComments() {
  const commentListElement = document.getElementById("comments-container");
  const loadingElement = createLoadingElement();
  commentListElement.appendChild(loadingElement);

  fetch("/data")
    .then((response) => response.json())
    .then((comments) => {
      comments.forEach((comment) => {
        commentListElement.appendChild(createCommentElement(comment));
      });
      commentListElement.removeChild(loadingElement);
    });
}

/** Creates an element that represents a comment, including its delete button. */
function createCommentElement(comment) {
  const commentElement = document.createElement("li");
  commentElement.className = "comment";

  const textElement = document.createElement("comment");
  textElement.innerText = comment.text;

  const dateElement = document.createElement("date");
  dateElement.innerText = comment.timestamp;
  
  const sentimentElement = document.createElement("date");
  sentimentElement.innerText = comment.sentiment;

  const deleteButtonElement = document.createElement("button");
  deleteButtonElement.innerText = "Delete";
  deleteButtonElement.addEventListener("click", () => {
    deleteComment(comment);

    // Remove the comment from the DOM.
    commentElement.remove();
  });

  commentElement.appendChild(textElement);
  commentElement.appendChild(dateElement);
  commentElement.appendChild(sentimentElement);
  commentElement.appendChild(deleteButtonElement);
  return commentElement;
}

function createLoadingElement() {
  const loadingElement = document.createElement("i");
  loadingElement.className = "fa fa-spinner fa-pulse fa-3x fa-fw";
  return loadingElement;
}

function stoppedTyping() {
  const input = document.getElementsByName("comment-input");
  if (input[0] != null) {
    const text = input[0].value;
    document.getElementById("submit_button").disabled = !/^(?!\s*$).+/.test(
      text
    );
  }
}

/** Tells the server to delete the comment. */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append("id", comment.id);
  fetch("/delete-comment", { method: "POST", body: params }).then((r) =>
    console.log(r)
  );
}
