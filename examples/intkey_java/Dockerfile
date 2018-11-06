# Copyright 2018 Bitwise IO
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ------------------------------------------------------------------------------

FROM maven:3-jdk-11

LABEL "install-type"="mounted"

EXPOSE 4004/tcp

RUN mkdir -p /project/sawtooth-sdk-java/ \
 && mkdir -p /var/log/sawtooth \
 && mkdir -p /var/lib/sawtooth \
 && mkdir -p /etc/sawtooth \
 && mkdir -p /etc/sawtooth/keys

ENV PATH=$PATH:/project/sawtooth-sdk-java/bin

WORKDIR /

RUN mkdir /build

COPY . /build/

RUN /build/bin/build_java_sdk

CMD /project/sawtooth-sdk-java/bin/build_java_intkey
