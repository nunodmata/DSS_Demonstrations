# Integração de novas funcionalidades da DSS Demo Webapp

Este projeto foi realizado no âmbito da Unidade Curricular de Engenharia de Segurança, integrada no perfil de Criptografia e Segurança da Informação.

## Introdução

Este projeto pretende tornar a aplicação mais completa do ponto de vista de opções para assinaturas digitais. Assim, as funcionalidades implementadas na aplicação web são as seguintes:

- Transpor as alterações realizadas anteriormente na versão antiga da DSS Demo WebApp para a versão mais recente, a fim de continuar a suportar a utilização com:

1. Cartão de Cidadão;
1. Chave Móvel Digital;
1. A fonte de timestamp do Cartão de Cidadão, de modo a não se utilizar a 'dummy timestamp source' que é empregada nas várias opções da Demo WebApp que utilizam timestamp.

- Adição de uma interface de autenticação inicial para realizar o login com usuário e senha;
- Integração de uma área de usuário, acessível apenas após a autenticação inicial, onde o usuário pode definir o número de telemóvel utilizado para a Chave Móvel Digital. Este número deve ser armazenado na Base de Dados;
- Sempre que o usuário realizar uma operação que utilize a Chave Móvel Digital, o número armazenado na Base de Dados para o respectivo usuário deve ser usado automaticamente;
- Por último, a possibilidade de assinar com chaves privadas (e respectivos certificados na hierarquia até à Entidade de Certificação raiz) em arquivo (formato DER), especificamente na funcionalidade de 'Sign a digest' nas opções de assinatura.

Adicionalmente, com a implementação destes pontos, foi dotada a aplicação web de suporte para a utilização da Chave Móvel Digital (CMD). A CMD, que é um dos padrões de assinatura e autenticação digital adotados pelo governo de Portugal, possui o mesmo valor legal que uma assinatura manuscrita e é reconhecida na União Europeia. Portugal, como membro da UE, segue as normas de Assinaturas Eletrónicas Avançadas (AdES) referidas pela ETSI (European Telecommunications Standards Institute) e aceita outros padrões de assinatura e autenticação digital.

Importa ainda salientar que, para além das funcionalidades descritas, e com o objetivo de melhorar a segurança do software, é necessário assegurar o uso de metodologias de software seguro. Destaca-se o processo de desenvolvimento de software criado pela Microsoft, o Microsoft Security Development Lifecycle (SDL). Este processo consiste numa série de práticas recomendadas e ferramentas que se concentram em segurança e privacidade, e são incorporadas em todas as fases do processo de desenvolvimento do software.
