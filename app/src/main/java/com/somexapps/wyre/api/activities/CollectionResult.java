package com.somexapps.wyre.api.activities;

import com.somexapps.wyre.api.tracks.SoundCloudTrackResult;

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
public class CollectionResult {
    private SoundCloudTrackResult origin;
    // TODO: Implement tags
    //private List<Tag> tags
    private String created_at;
    private String type;

    public SoundCloudTrackResult getOrigin() {
        return origin;
    }

    public void setOrigin(SoundCloudTrackResult origin) {
        this.origin = origin;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
