/*
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package handlers

import (
	"github.com/OpenNMS/opennms-operator/internal/model/values"
	"github.com/OpenNMS/opennms-operator/internal/util/yaml"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type MailServerHandler struct {
	ServiceHandlerObject
}

func (h *MailServerHandler) UpdateConfig(values values.TemplateValues) {
	var service corev1.Service
	var deployment appsv1.Deployment

	yaml.LoadYaml(filepath("mail-server/mail-server-service.yaml"), values, &service)
	yaml.LoadYaml(filepath("mail-server/mail-server-deployment.yaml"), values, &deployment)

	h.Config = []client.Object{
		&service,
		&deployment,
	}
}
