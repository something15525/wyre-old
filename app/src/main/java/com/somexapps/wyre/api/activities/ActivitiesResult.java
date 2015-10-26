package com.somexapps.wyre.api.activities;

import java.util.List;

/**
 * Copyright 2015 Michael Limb
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ActivitiesResult {
    private String future_href;
    private List<CollectionResult> collection;
    private String next_href;

    public String getFuture_href() {
        return future_href;
    }

    public void setFuture_href(String future_href) {
        this.future_href = future_href;
    }

    public List<CollectionResult> getCollection() {
        return collection;
    }

    public void setCollection(List<CollectionResult> collection) {
        this.collection = collection;
    }

    public String getNext_href() {
        return next_href;
    }

    public void setNext_href(String next_href) {
        this.next_href = next_href;
    }
}
